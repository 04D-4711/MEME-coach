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
    private final LLMClient client;

    //provide local feedback when not connecting ai
    private final FeedbackService fallback;

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
        QuizFeedback local =
                fallback.generateFeedback(request, persona);

        String systemPrompt =
                "You are a friendly quiz tutor. "
                        + persona.getSystemPromptStyle()
                        + "\nExplain why the provided correct answer is correct. "
                        + "Use simple English and no more than three sentences. "
                        + "Do not invent a different correct answer. "
                        + "Do not insult the student. "
                        + "Return only the explanation.";

        String userPrompt =
                "Question: " + request.question()
                        + "\nStudent answer: " + request.selectedAnswer()
                        + "\nCorrect answer: " + request.correctAnswer()
                        + "\nReference explanation: "
                        + request.correctExplanation();

        try {
            String explanation = client.requestExplanation(
                    systemPrompt,
                    userPrompt
            );

            if (explanation == null || explanation.isBlank()) {
                return local;
            }

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
            Thread.currentThread().interrupt();
            return local;

        } catch (Exception e) {
            return local;
        }
    }
}

