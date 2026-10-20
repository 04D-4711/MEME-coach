package test;

import attachments.Quiz;
import dao.PostDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import memecoach.DemoExplanationProvider;
import memecoach.FeedbackCallback;
import memecoach.FeedbackPersona;
import memecoach.FeedbackPersonaFactory;
import memecoach.FeedbackRequest;
import memecoach.MemeCoachManager;
import memecoach.QuizFeedback;
import memecoach.TemplateFeedbackService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;

/**
 * @author Jie Ren
 * @version 1.0
 * Utilize the real Persona, TemplateFeedbackService, ExplanationProvider,
 * and Manager.
 * Test the complete business chain of Meme Coach when not connected to LLM.
 * Unlike MemeCoachTests with person 2, this type does not use fake
 * Persona/fake FeedbackService.
 * This test only verifies the Java end, not the Android drawing and disk storage.
 */

public class MemeCoachIntegrationTests {
    private static final String QUESTION =
            "Which pattern hides a subsystem behind one class?";
    private static final String[] OPTIONS = {
            "Facade", "Singleton", "Observer"
    };
    private static final String EXPLANATION =
            "A Facade offers one simple entry point.";

    //During the test, make the two Executors execute tasks immediately to
    // avoid waiting for the asynchronous threads.
    //When developing for the Android side, this Executor cannot be
    // directly used as the UI main thread executor.
    private static final Executor DIRECT = Runnable::run;

    private User user;

    @Before
    public void setUp() {
        user = new User(UUID.randomUUID(), User.Role.Member, "tester", "pass");
        PostDAO.getInstance().clear();
    }

    private Quiz newQuiz() {
        //The correct answer is index 0 (Facade),
        // while index 2 (Observer) is the incorrect option.
        return new Quiz(QUESTION, OPTIONS, 0);
    }

    private MemeCoachManager newManager(Map<String, String> explanations) {
        //Using the actual implementation class and the four-parameter
        // constructor method, execute the actual Factory selection process.
        return new MemeCoachManager(
                new TemplateFeedbackService(new Random(7)),
                new DemoExplanationProvider(explanations),
                DIRECT,
                DIRECT
        );
    }

    @Test
    public void factoryCreatesAllThreeRealPersonas() {
        //Verify that the personas created by the Factory can indeed provide different image keys.
        String[] ids = {"supportive", "penguin", "teacher"};
        String[] imageKeys = {"meme_supportive", "meme_penguin", "meme_teacher"};

        for (int i = 0; i < ids.length; i++) {
            FeedbackPersona persona = FeedbackPersonaFactory.makePersona(ids[i]);
            assertEquals(ids[i], persona.getPersonaId());
            assertEquals(imageKeys[i], persona.getImageKey());
            assertFalse(persona.getPersonaName().isBlank());
            assertFalse(persona.getSystemPromptStyle().isBlank());
        }
    }

    @Test
    public void factoryRejectsUnknownPersona() {
        //One cannot unknowingly transform into someone else's personality without realizing it.
        assertThrows(IllegalArgumentException.class,
                () -> FeedbackPersonaFactory.makePersona("dragon"));
        assertThrows(IllegalArgumentException.class,
                () -> FeedbackPersonaFactory.makePersona(null));
    }

    @Test
    public void templateChoosesAReproducibleLocalHeadline() {
        //After setting the random seed, the same
        // Persona will return the same transcript for the same request.
        FeedbackRequest request = new FeedbackRequest(
                QUESTION, "Observer", "Facade", EXPLANATION
        );
        FeedbackPersona persona = FeedbackPersonaFactory.makePersona("penguin");

        QuizFeedback first = new TemplateFeedbackService(new Random(12))
                .generateFeedback(request, persona);
        QuizFeedback second = new TemplateFeedbackService(new Random(12))
                .generateFeedback(request, persona);

        assertEquals(first.headline(), second.headline());
        assertFalse(first.headline().isBlank());
        assertEquals("penguin", first.personaId());
        assertEquals("meme_penguin", first.imageKey());
        assertEquals(EXPLANATION, first.explanation());
    }

    @Test
    public void explanationProviderLooksUpAndCopiesItsMap() {
        //The Map must be copied to prevent other code from later
        // modifying the Map and thereby affecting the existing Provider.
        Map<String, String> source = new HashMap<>();
        source.put(QUESTION, EXPLANATION);
        DemoExplanationProvider provider = new DemoExplanationProvider(source);

        source.put(QUESTION, "Changed after construction");

        assertEquals(EXPLANATION, provider.getExplanation(QUESTION));
        assertEquals("", provider.getExplanation("Unknown question"));
        assertEquals("", provider.getExplanation(null));
    }

    @Test
    public void wrongAnswerRunsTheRealFeedbackPipeline() {
        Quiz quiz = newQuiz();
        RecordingCallback callback = new RecordingCallback();

        // 实际流程：Manager 判错 -> Factory -> Provider -> Template -> Callback。
        newManager(Map.of(QUESTION, EXPLANATION))
                .submitAnswer(quiz, user, 2, "penguin", callback);

        assertEquals(List.of("feedback"), callback.events);
        assertNotNull(callback.feedback);
        assertEquals("penguin", callback.feedback.personaId());
        assertEquals("meme_penguin", callback.feedback.imageKey());
        assertEquals(QUESTION, callback.feedback.question());
        assertEquals("Observer", callback.feedback.selectedAnswer());
        assertEquals("Facade", callback.feedback.correctAnswer());
        assertEquals(EXPLANATION, callback.feedback.explanation());
        assertFalse(callback.feedback.headline().isBlank());
        assertEquals(Integer.valueOf(2), quiz.getResponses().get(user.id()));
    }

    @Test
    public void correctAnswerOnlyCallsOnCorrect() {
        Quiz quiz = newQuiz();
        RecordingCallback callback = new RecordingCallback();

        //Correct answers do not generate Meme Feedback.
        newManager(Map.of(QUESTION, EXPLANATION))
                .submitAnswer(quiz, user, 0, "teacher", callback);

        assertEquals(List.of("correct"), callback.events);
        assertNull(callback.feedback);
        assertEquals(Integer.valueOf(0), quiz.getResponses().get(user.id()));
    }

    @Test
    public void missingExplanationUsesGenericFallback() {
        RecordingCallback callback = new RecordingCallback();

        //When the title is not in the preset library, there should still be a
        // displayable general feedback instead of throwing an exception.
        newManager(Map.of()).submitAnswer(newQuiz(), user, 2, "supportive", callback);

        assertEquals(List.of("feedback"), callback.events);
        assertNotNull(callback.feedback);
        assertFalse(callback.feedback.explanation().isBlank());
        assertEquals("Facade", callback.feedback.correctAnswer());
    }

    @Test
    public void unknownPersonaAndInvalidOptionDoNotRecordAnAnswer() {
        Quiz quiz = newQuiz();
        RecordingCallback callback = new RecordingCallback();
        MemeCoachManager manager = newManager(Map.of(QUESTION, EXPLANATION));

        manager.submitAnswer(quiz, user, 2, "dragon", callback);
        manager.submitAnswer(quiz, user, -1, "penguin", callback);

        assertEquals(List.of("error", "error"), callback.events);
        assertTrue(quiz.getResponses().isEmpty());
    }

    @Test
    public void repeatedAnswerIsRejectedAndOriginalAnswerRemains() {
        Quiz quiz = newQuiz();
        RecordingCallback callback = new RecordingCallback();
        MemeCoachManager manager = newManager(Map.of(QUESTION, EXPLANATION));

        manager.submitAnswer(quiz, user, 2, "teacher", callback);
        manager.submitAnswer(quiz, user, 0, "teacher", callback);

        assertEquals(List.of("feedback", "error"), callback.events);
        assertEquals(Integer.valueOf(2), quiz.getResponses().get(user.id()));
    }

    @Test
    public void realFeedbackCanBeSharedToPostDAO() {
        Quiz quiz = newQuiz();
        RecordingCallback callback = new RecordingCallback();
        MemeCoachManager manager = newManager(Map.of(QUESTION, EXPLANATION));

        manager.submitAnswer(quiz, user, 2, "penguin", callback);
        assertNotNull(callback.feedback);

        //ensure text include correct answer,
        //post itself only store text, without image
        String shareText = callback.feedback.shareText();
        assertTrue(shareText.contains("Correct answer: Facade"));

        Post post = manager.shareFeedback(user, callback.feedback);
        assertNotNull(post);
        assertSame(post, PostDAO.getInstance().get(new Post(post.id)));
        assertEquals(user.id(), post.poster);

        Iterator<Message> messages = post.messages.getAll();
        assertTrue(messages.hasNext());
        assertEquals(shareText, messages.next().message());
        assertFalse(messages.hasNext());
    }

    //Collect the callback from the Manager to facilitate comparison of which branch was actually executed.
    private static class RecordingCallback implements FeedbackCallback {
        private final List<String> events = new ArrayList<>();
        private QuizFeedback feedback;

        @Override
        public void onCorrect() {
            events.add("correct");
        }

        @Override
        public void onFeedback(QuizFeedback feedback) {
            events.add("feedback");
            this.feedback = feedback;
        }

        @Override
        public void onError(String message) {
            events.add("error");
        }
    }
}

