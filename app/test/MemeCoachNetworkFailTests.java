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
 Test the AI success rate, API failure rate and offline fallback of Meme Coach.
 *
 Here, a simulated LLMClient is used and no actual requests are sent to OpenAI.
 Therefore, the test run will not incur API charges and there is no need to start the Python backend.
 */
public class MemeCoachNetworkFailTests {

    private static final String QUESTION = "What does DAO stand for?";
    private static final String WRONG = "Data Analysis Object";
    private static final String CORRECT = "Data Access Object";
    private static final String REFERENCE =
            "DAO separates data access from business logic.";

    private FeedbackRequest request() {
        return new FeedbackRequest(QUESTION, WRONG, CORRECT, REFERENCE);
    }
    private FeedbackService fallback() {
        return new TemplateFeedbackService(new Random(7));
    }
    private FeedbackPersona penguin() {
        return FeedbackPersonaFactory.makePersona("penguin");
    }

    @Test
    public void apiExceptionFallsBackToLocalExplanation() {
        LLMClient brokenClient = (systemPrompt, userPrompt) -> {
            throw new IOException("Simulated network failure");
        };

        FeedbackService service = new LLMFeedbackService(brokenClient, fallback());
        QuizFeedback result = service.generateFeedback(request(), penguin());

        assertNotNull(result);
        assertEquals(REFERENCE, result.explanation());
        assertEquals("penguin", result.personaId());
        assertEquals("meme_penguin", result.imageKey());
        assertTrue(!result.headline().isBlank());
    }

    @Test
    public void emptyApiResponseFallsBackToLocalExplanation() {
        LLMClient emptyClient = (systemPrompt, userPrompt) -> "   ";

        FeedbackService service = new LLMFeedbackService(emptyClient, fallback());
        QuizFeedback result = service.generateFeedback(request(), penguin());

        assertEquals(REFERENCE, result.explanation());
    }

    @Test
    public void successfulApiResponseReplacesOnlyExplanation() {
        String aiExplanation =
                "DAO is an abstraction for accessing stored data.";
        LLMClient workingClient = (systemPrompt, userPrompt) -> aiExplanation;

        FeedbackService service = new LLMFeedbackService(workingClient, fallback());
        QuizFeedback result = service.generateFeedback(request(), penguin());

        assertEquals(aiExplanation, result.explanation());
        assertEquals("meme_penguin", result.imageKey());
        assertEquals(CORRECT, result.correctAnswer());
        assertTrue(!result.headline().isBlank());
    }

    @Test
    public void managerStillReturnsFeedbackWhenApiIsUnavailable() {
        Quiz quiz = new Quiz(
                QUESTION,
                new String[]{WRONG, CORRECT, "Data Analysis Option"},
                1
        );
        User user = new User(UUID.randomUUID(), User.Role.Member, "tester", "pass");

        LLMClient brokenClient = (systemPrompt, userPrompt) -> {
            throw new IOException("Server unavailable");
        };

        MemeCoachManager manager = new MemeCoachManager(
                new LLMFeedbackService(brokenClient, fallback()),
                new DemoExplanationProvider(Map.of(QUESTION, REFERENCE)),
                Runnable::run,  // 仅在 Java 测试中同步运行
                Runnable::run
        );

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

        assertNull(error.get());
        assertNotNull(received.get());
        assertEquals(REFERENCE, received.get().explanation());
        assertEquals("meme_penguin", received.get().imageKey());
        assertEquals(Integer.valueOf(0), quiz.getResponses().get(user.id()));
    }
}

