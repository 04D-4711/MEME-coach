
import os
import secrets
import time

from collections import deque
from pathlib import Path
from threading import Lock

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Header, Depends
from openai import OpenAI
from pydantic import BaseModel, Field


# read local .env configuration
# OpenAI API Key And both the temporary access password and the other information are stored here.
load_dotenv(Path(__file__).with_name(".env"))

# my server
app = FastAPI()


# Data format sent by the Java client
class ExplanationRequest(BaseModel):
    systemPrompt: str = Field(min_length=1, max_length=2000)
    userPrompt: str = Field(min_length=1, max_length=4000)


# The data format returned by the backend
class ExplanationResponse(BaseModel):
    explanation: str


# A maximum of 6 valid requests are allowed per minute.
MAX_REQUESTS_PER_MINUTE = 6

# During the operation of this server, a maximum of 30 valid requests are permitted.
MAX_TOTAL_REQUESTS = 30

# Save the request time within the last minute.
request_times = deque()

# Save the total number of requests made during this run.
total_requests = 0

# Prevent multiple requests from simultaneously modifying the counter
request_lock = Lock()


def verify_demo_access(
        x_demo_token: str | None = Header(default=None)
):
    """
    Verify the identity of the client and limit the number of AI calls.

    FastAPI will automatically associate x_demo_token with
    the HTTP request header X-Demo-Token.
    """

    global total_requests

    # Step 1: Read the temporary password configured by the server side
    expected_token = os.getenv("DEMO_API_TOKEN", "")

    #If an effective password is not configured, all AI requests will be denied.
    if len(expected_token) < 32:
        raise HTTPException(
            status_code=503,
            detail="Demo access is not configured"
        )

    # Step 2: Verify the password provided by the client
    # The function "compare_digest" is used for securely comparing strings
    if (
            not x_demo_token
            or not secrets.compare_digest(
        x_demo_token, expected_token
    )
    ):
        raise HTTPException(
            status_code=401,
            detail="Unauthorized"
        )

    # Step 3: Check the call frequency
    now = time.monotonic()

    with request_lock:

        while (
                request_times
                and now - request_times[0] >= 60
        ):
            request_times.popleft()

        if (
                len(request_times) >= MAX_REQUESTS_PER_MINUTE
                or total_requests >= MAX_TOTAL_REQUESTS
        ):
            raise HTTPException(
                status_code=429,
                detail="Demo request limit reached"
            )

        request_times.append(now)
        total_requests += 1


# Health check will not consume the OpenAI API quota
@app.get("/health")
def health():
    return {"status": "ok"}


# AI Explanation Interface: Access must be verified first
@app.post(
    "/api/explain",
    response_model=ExplanationResponse,
    dependencies=[Depends(verify_demo_access)]
)
def explain(request: ExplanationRequest):

    api_key = os.getenv("OPENAI_API_KEY")

    if not api_key or api_key == "YOUR_OWN_API_KEY":
        raise HTTPException(
            status_code=503,
            detail="OpenAI API key is not configured"
        )

    try:
        client = OpenAI(
            api_key=api_key,
            timeout=20.0,
            max_retries=0
        )

        response = client.responses.create(
            model=os.getenv("OPENAI_MODEL", "gpt-6-luna"),
            instructions=request.systemPrompt,
            input=request.userPrompt,
            reasoning={"effort": "none"},
            max_output_tokens=250,
            store=False
        )

        explanation = (response.output_text or "").strip()

        if not explanation:
            raise HTTPException(
                status_code=502,
                detail="AI returned an empty explanation"
            )

        return ExplanationResponse(explanation=explanation)

    except HTTPException:
        raise

    except Exception:
        raise HTTPException(
            status_code=502,
            detail="AI service is temporarily unavailable"
        )
