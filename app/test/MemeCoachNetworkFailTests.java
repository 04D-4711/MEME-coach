package test;

import attachments.Quiz;
import dao.model.User;
import memecoach.DemoExplanationProvider;
import memecoach.FeedbackCallback;
import memecoach.FeedbackPersona;
import memecoach.FeedbackPersonaFactory;
import memecoach.FeedbackRequest;
import memecoach.FeedbackService;
import memecoach.LLMClient;
import memecoach.LLMFeedbackService;
import memecoach.MemeCoachManager;
import memecoach.QuizFeedback;
import memecoach.TemplateFeedbackService;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 测试 Meme Coach 的 AI 成功、API 失败和离线回退。
 *
 * 这里使用模拟 LLMClient，不向 OpenAI 发送真实请求，
 * 因此运行测试不会产生 API 费用，也不需要启动 Python 后端。
 */
public class MemeCoachNetworkFailTests {

    private static final String QUESTION = "What does DAO stand for?";
    private static final String WRONG = "Data Analysis Object";
    private static final String CORRECT = "Data Access Object";
    private static final String REFERENCE =
            "DAO separates data access from business logic.";

    /** 使用一个固定的输入，避免不同测试结果不一致。 */
    private FeedbackRequest request() {
        return new FeedbackRequest(QUESTION, WRONG, CORRECT, REFERENCE);
    }

    /** 每个测试使用独立的本地服务和 Random，避免测试相互影响。 */
    private FeedbackService fallback() {
        return new TemplateFeedbackService(new Random(7));
    }

    /** 使用实际的企鹅策略，不创建假的 Persona。 */
    private FeedbackPersona penguin() {
        return FeedbackPersonaFactory.makePersona("penguin");
    }

    @Test
    public void apiExceptionFallsBackToLocalExplanation() {
        // 模拟网络断开：调用客户端时抛出 IOException
        LLMClient brokenClient = (systemPrompt, userPrompt) -> {
            throw new IOException("Simulated network failure");
        };

        FeedbackService service = new LLMFeedbackService(brokenClient, fallback());
        QuizFeedback result = service.generateFeedback(request(), penguin());

        // API 失败后仍应返回完整的本地反馈，不抛异常
        assertNotNull(result);
        assertEquals(REFERENCE, result.explanation());
        assertEquals("penguin", result.personaId());
        assertEquals("meme_penguin", result.imageKey());
        assertTrue(!result.headline().isBlank());
    }

    @Test
    public void emptyApiResponseFallsBackToLocalExplanation() {
        // 模拟服务器成功响应，但 LLM 返回空白解释
        LLMClient emptyClient = (systemPrompt, userPrompt) -> "   ";

        FeedbackService service = new LLMFeedbackService(emptyClient, fallback());
        QuizFeedback result = service.generateFeedback(request(), penguin());

        // 仍然应该使用预设解释
        assertEquals(REFERENCE, result.explanation());
    }

    @Test
    public void successfulApiResponseReplacesOnlyExplanation() {
        // 模拟 LLM 成功返回更具体的知识解释
        String aiExplanation =
                "DAO is an abstraction for accessing stored data.";
        LLMClient workingClient = (systemPrompt, userPrompt) -> aiExplanation;

        FeedbackService service = new LLMFeedbackService(workingClient, fallback());
        QuizFeedback result = service.generateFeedback(request(), penguin());

        // 只有解释变成 AI 文本，原有图片、语录和正确答案仍保留
        assertEquals(aiExplanation, result.explanation());
        assertEquals("meme_penguin", result.imageKey());
        assertEquals(CORRECT, result.correctAnswer());
        assertTrue(!result.headline().isBlank());
    }

    @Test
    public void managerStillReturnsFeedbackWhenApiIsUnavailable() {
        // 这次从真实 Quiz 和人员 2 的 Manager 入口测试整条离线路径
        Quiz quiz = new Quiz(
                QUESTION,
                new String[]{WRONG, CORRECT, "Data Analysis Option"},
                1
        );
        User user = new User(UUID.randomUUID(), User.Role.Member, "tester", "pass");

        // 不连接服务器，模拟 API 请求失败
        LLMClient brokenClient = (systemPrompt, userPrompt) -> {
            throw new IOException("Server unavailable");
        };

        MemeCoachManager manager = new MemeCoachManager(
                new LLMFeedbackService(brokenClient, fallback()),
                new DemoExplanationProvider(Map.of(QUESTION, REFERENCE)),
                Runnable::run,  // 仅在 Java 测试中同步运行
                Runnable::run
        );

        // 使用回调捕获人员 2 的 Manager 返回的结果
        AtomicReference<QuizFeedback> received = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        manager.submitAnswer(quiz, user, 0, "penguin", new FeedbackCallback() {
            @Override
            public void onCorrect() {
                error.set("Wrong answer was incorrectly marked correct");
            }

            @Override
            public void onFeedback(QuizFeedback feedback) {
                received.set(feedback);
            }

            @Override
            public void onError(String message) {
                error.set(message);
            }
        });

        // 即使 AI 不可用，Manager 仍应正常返回 Meme Feedback
        assertNull(error.get());
        assertNotNull(received.get());
        assertEquals(REFERENCE, received.get().explanation());
        assertEquals("meme_penguin", received.get().imageKey());
        assertEquals(Integer.valueOf(0), quiz.getResponses().get(user.id()));
    }
}

