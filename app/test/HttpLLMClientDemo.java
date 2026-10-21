package test;

import memecoach.HttpLLMClient;
import memecoach.LLMClient;

/**
 Manual connectivity check: It is called only when the Python backend is running.
 Note: Calling the actual OpenAI API incurs charges, so do not include it in the automated test suite.
 */
public class HttpLLMClientDemo {
    public static void main(String[] args) throws Exception {
        // Both IntelliJ and the backend are running on the same computer and are using 127.0.0.1.
        LLMClient client = new HttpLLMClient(
                "http://127.0.0.1:8000/api/explain"
        );

        String explanation = client.requestExplanation(
                "You are a friendly quiz teacher. Explain in two short sentences.",
                "Question: What does DAO stand for?\n"
                        + "Student answer: Data Analysis Object\n"
                        + "Correct answer: Data Access Object"
        );

        System.out.println("AI explanation: " + explanation);
    }
}
