package test;

import memecoach.HttpLLMClient;
import memecoach.LLMClient;

/**
 * 手动运行的联通性检查：仅当 Python 后端正在运行时调用。
 * 注意：调用真实 OpenAI API 会产生费用，所以不要放进自动化测试套件。
 */
public class HttpLLMClientDemo {
    public static void main(String[] args) throws Exception {
        // IntelliJ 和后端都在同一台电脑上运行，使用 127.0.0.1。
        LLMClient client = new HttpLLMClient(
                "http://127.0.0.1:8000/api/explain"
        );

        // 模拟 MemeCoachManager 提供的 Prompt。
        String explanation = client.requestExplanation(
                "You are a friendly quiz teacher. Explain in two short sentences.",
                "Question: What does DAO stand for?\n"
                        + "Student answer: Data Analysis Object\n"
                        + "Correct answer: Data Access Object"
        );

        System.out.println("AI explanation: " + explanation);
    }
}
