package memecoach;

import java.util.Objects;

/**
 * @author Jie Ren
 * @version 1.0
 * 使用大语言模型生成Quiz错误反馈。
 *
 * 实现FeedbackService接口。
 *
 * 如果 LLM 调用失败，自动使用本地反馈服务。
 * 这样即使没有网络，Meme Coach也能正常工作。
 * (可是中文真的太好用了你知道吗）
 */

public class LLMFeedbackService implements FeedbackService {
    //用于请求AI的客户端
    private final LLMClient client;

    //当AI无法使用时，提供本地反馈
    private final FeedbackService fallback;

    /**
     * 通过依赖注入接收客户端和备用服务。
     *
     * 不直接创建某一种具体AI客户端，
     * 方便未来替换不同模型或服务商。
     */
    public LLMFeedbackService(
            LLMClient client,
            FeedbackService fallback
    ) {
        this.client = Objects.requireNonNull(client);
        this.fallback = Objects.requireNonNull(fallback);
    }

    @Override
    public QuizFeedback generateFeedback(
            FeedbackRequest request,
            FeedbackPersona persona
    ) {
        //第一步：先准备本地反馈
        //保证即使AI失败也有可展示的内容
        QuizFeedback local =
                fallback.generateFeedback(request, persona);

        //第二步：创建 System Prompt
        //加入用户选择的人设风格
        String systemPrompt =
                "You are a friendly quiz tutor. "
                        + persona.getSystemPromptStyle()
                        + "\nExplain why the provided correct answer is correct. "
                        + "Use simple English and no more than three sentences. "
                        + "Do not invent a different correct answer. "
                        + "Do not insult the student. "
                        + "Return only the explanation.";

        //第三步：创建User Prompt
        //提供题目、错误答案和正确答案
        String userPrompt =
                "Question: " + request.question()
                        + "\nStudent answer: " + request.selectedAnswer()
                        + "\nCorrect answer: " + request.correctAnswer()
                        + "\nReference explanation: "
                        + request.correctExplanation();

        try {
            //第四步请求真正的AI解释
            String explanation = client.requestExplanation(
                    systemPrompt,
                    userPrompt
            );

            //AI返回空内容时，使用本地反馈
            if (explanation == null || explanation.isBlank()) {
                return local;
            }

            //第五步用AI解释替换本地解释
            //其他字段继续使用原来的 Persona 和 Meme 图片
            return new QuizFeedback(
                    local.personaId(),
                    local.personaName(),
                    local.headline(),
                    explanation.trim(),
                    local.imageKey(),
                    local.question(),
                    local.selectedAnswer(),
                    local.correctAnswer()
            );

        } catch (InterruptedException e) {
            //恢复线程中断状态
            Thread.currentThread().interrupt();
            return local;

        } catch (Exception e) {
            //网络、API 或其他请求错误时回退到本地服务
            return local;
        }
    }
}

