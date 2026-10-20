
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


# 读取本地 .env 配置
# OpenAI API Key 和临时访问口令都保存在这里
load_dotenv(Path(__file__).with_name(".env"))

# 创建后端服务器
app = FastAPI()


# Java 客户端发送的数据格式
class ExplanationRequest(BaseModel):
    systemPrompt: str = Field(min_length=1, max_length=2000)
    userPrompt: str = Field(min_length=1, max_length=4000)


# 后端返回的数据格式
class ExplanationResponse(BaseModel):
    explanation: str


# 每分钟最多允许 6 次有效请求
MAX_REQUESTS_PER_MINUTE = 6

# 本次服务器运行期间最多允许 30 次有效请求
MAX_TOTAL_REQUESTS = 30

# 保存最近一分钟内的请求时间
request_times = deque()

# 保存本次运行期间的请求总数
total_requests = 0

# 防止多个请求同时修改计数器
request_lock = Lock()


def verify_demo_access(
        x_demo_token: str | None = Header(default=None)
):
    """
    验证客户端身份，并限制 AI 调用次数。

    FastAPI 会自动把 x_demo_token
    对应到 HTTP 请求头 X-Demo-Token。
    """

    global total_requests

    # 第一步：读取服务端配置的临时口令
    expected_token = os.getenv("DEMO_API_TOKEN", "")

    # 没有配置有效口令时，拒绝所有 AI 请求
    if len(expected_token) < 32:
        raise HTTPException(
            status_code=503,
            detail="Demo access is not configured"
        )

    # 第二步：检查客户端提供的口令
    # compare_digest 用于安全比较字符串
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

    # 第三步：检查调用频率
    now = time.monotonic()

    with request_lock:

        # 删除一分钟之前的请求记录
        while (
                request_times
                and now - request_times[0] >= 60
        ):
            request_times.popleft()

        # 检查每分钟次数及累计次数
        if (
                len(request_times) >= MAX_REQUESTS_PER_MINUTE
                or total_requests >= MAX_TOTAL_REQUESTS
        ):
            raise HTTPException(
                status_code=429,
                detail="Demo request limit reached"
            )

        # 记录本次已授权请求
        request_times.append(now)
        total_requests += 1


# 健康检查，不会消耗 OpenAI API 额度
@app.get("/health")
def health():
    return {"status": "ok"}


# AI 解释接口：必须先通过访问验证
@app.post(
    "/api/explain",
    response_model=ExplanationResponse,
    dependencies=[Depends(verify_demo_access)]
)
def explain(request: ExplanationRequest):

    # 从环境变量读取 OpenAI API Key
    api_key = os.getenv("OPENAI_API_KEY")

    if not api_key or api_key == "YOUR_OWN_API_KEY":
        raise HTTPException(
            status_code=503,
            detail="OpenAI API key is not configured"
        )

    try:
        # 创建 OpenAI 客户端
        client = OpenAI(
            api_key=api_key,
            timeout=20.0,
            max_retries=0
        )

        # 真正调用 OpenAI Responses API
        response = client.responses.create(
            model=os.getenv("OPENAI_MODEL", "gpt-6-luna"),
            instructions=request.systemPrompt,
            input=request.userPrompt,
            reasoning={"effort": "none"},
            max_output_tokens=250,
            store=False
        )

        # 读取 AI 的解释
        explanation = (response.output_text or "").strip()

        if not explanation:
            raise HTTPException(
                status_code=502,
                detail="AI returned an empty explanation"
            )

        # 返回给 Java 的 JSON
        return ExplanationResponse(explanation=explanation)

    except HTTPException:
        raise

    except Exception:
        # 不向外部暴露 API 的内部错误细节
        raise HTTPException(
            status_code=502,
            detail="AI service is temporarily unavailable"
        )
