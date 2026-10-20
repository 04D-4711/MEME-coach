
import os
from pathlib import Path

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from openai import OpenAI
from pydantic import BaseModel, Field


#读取backend/.env中的环境变量
load_dotenv(Path(__file__).with_name(".env"))

#创建FastAPI应用
app = FastAPI()


#定义Java客户端发送过来的JSON数据格式
class ExplanationRequest(BaseModel):
    systemPrompt: str = Field(min_length=1, max_length=2000)
    userPrompt: str = Field(min_length=1, max_length=4000)


#定义服务器返回给Java客户端的数据格式
class ExplanationResponse(BaseModel):
    explanation: str


#原有的服务器健康检查接口
@app.get("/health")
def health():
    return {"status": "ok"}


#新增的AI解释接口
@app.post("/api/explain", response_model=ExplanationResponse)
def explain(request: ExplanationRequest):

    #第一步：从环境变量读取APIKey
    api_key = os.getenv("OPENAI_API_KEY")

    #没有配置有效的Key时，返回服务器错误
    if not api_key or api_key == "YOUR_OWN_API_KEY":
        raise HTTPException(
            status_code=503,
            detail="OpenAI API key is not configured"
        )

    try:
        #第二步：创建OpenAI客户端
        #限制超时时间，避免Demo长时间等待
        client = OpenAI(
            api_key=api_key,
            timeout=20.0,
            max_retries=0
        )

        #第三步：调用OpenAI Responses API
        response = client.responses.create(
            model=os.getenv("OPENAI_MODEL", "gpt-6-luna"),
            instructions=request.systemPrompt,
            input=request.userPrompt,
            reasoning={"effort": "none"},
            max_output_tokens=250,
            store=False
        )

        #第四步：获取AI生成的文本
        explanation = (response.output_text or "").strip()

        #如果AI没有生成有效内容，则返回错误
        if not explanation:
            raise HTTPException(
                status_code=502,
                detail="AI returned an empty explanation"
            )

        #第五步：把AI解释返回给Java客户端
        return ExplanationResponse(explanation=explanation)

    except HTTPException:
        raise

    except Exception:
        # 不将密钥或内部异常细节暴露给调用者
        raise HTTPException(
            status_code=502,
            detail="AI service is temporarily unavailable"
        )
