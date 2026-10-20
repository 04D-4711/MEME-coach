package test;

import memecoach.HttpLLMClient;
import memecoach.LLMClient;

/**
 * 手动检查跨电脑 HTTPS + Demo Token + OpenAI 调用。
 * 运行前在 IntelliJ Run Configuration 中设置以下环境变量：
 * MEMECOACH_API_URL=https://...trycloudflare.com/api/explain
 * DEMO_API_TOKEN=（Python 后端 .env 中配置的临时口令）
 *
 * 注意：每次运行都会进行一次可能计费的真实 API 请求。
 */
public class LLMRemoteDemo {

    public static void main(String[] args) throws Exception {
        // 从当前进程的环境变量读取连接信息，避免把 URL/Token 写进源码。
        String endpoint = System.getenv("MEMECOACH_API_URL");
        String token = System.getenv("DEMO_API_TOKEN");

        if (endpoint == null || !endpoint.startsWith("https://")) {
            throw new IllegalStateException("Please set MEMECOACH_API_URL to the tunnel HTTPS /api/explain URL");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Please set DEMO_API_TOKEN in the Run Configuration");
        }

        // Java 只拿到临时口令，不接触服务端 OpenAI API Key。
        LLMClient client = new HttpLLMClient(endpoint, token);
        String explanation = client.requestExplanation(
                "You are a friendly quiz teacher. Explain in two short sentences.",
                "Question: What does DAO stand for?\n"
                        + "Student answer: Data Analysis Object\n"
                        + "Correct answer: Data Access Object"
        );

        System.out.println("Remote AI explanation: " + explanation);
        System.out.println("SUCCESS: Java -> HTTPS Tunnel -> Python -> OpenAI");
    }
}
