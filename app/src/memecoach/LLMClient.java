package memecoach;

/**
 * LLM客户端接口。
 * 作用：定义Meme Coach如何向大语言模型请求解释。
 *
 * 当前接口本身不执行HTTP请求，
 * 也不保存任何APIKey。
 * 以后由具体的客户端实现这个接口，
 * 将请求发送到我们自己的后端服务器。
 *
 * 这样LLMFeedbackService不需要知道：
 * 1.使用哪一个AI模型
 * 2.后端服务器部署在哪里
 * 3.网络请求具体如何实现
 * 体现Dependency Inversion Principle。
 */

public interface LLMClient {

    /**
     * 请AI生成Quiz的知识解释。
     *
     * @param systemPrompt AI的角色和回答风格
     * @param userPrompt Quiz题目、用户答案和正确答案
     * @return AI生成的解释文本
     * @throws Exception 当网络或API请求失败时抛出异常
     */
    String requestExplanation(
            String systemPrompt,
            String userPrompt
    ) throws Exception;
}

