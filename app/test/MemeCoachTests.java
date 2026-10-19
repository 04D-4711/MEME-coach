package test;

import attachments.Quiz;
import dao.PostDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import memecoach.FeedbackCallback;
import memecoach.FeedbackPersona;
import memecoach.FeedbackPostFactory;
import memecoach.FeedbackRequest;
import memecoach.FeedbackService;
import memecoach.MemeCoachManager;
import memecoach.QuizExplanationProvider;
import memecoach.QuizFeedback;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MemeCoachTests {
    private static final String QUESTION = "Which pattern hides a subsystem behind one class?";
    private static final String[] OPTIONS = {"Facade", "Singleton", "Observer"};
    private static final int CORRECT = 0;
    private static final int WRONG = 2;
    private static final String EXPLANATION = "A Facade offers one simple entry point.";

    private static final Executor DIRECT = Runnable::run;

    private final FeedbackPersona penguin = new StubPersona("penguin", "Penguin", "meme_penguin");
    private final QuizExplanationProvider explanations = question -> Map.of(QUESTION, EXPLANATION).getOrDefault(question, "");
    private final FeedbackService service = (request, persona) -> new QuizFeedback(
            persona.getPersonaId(), persona.getPersonaName(), persona.makeHeadline(request, new Random(0)),
            request.correctExplanation(), persona.getImageKey(),
            request.question(), request.selectedAnswer(), request.correctAnswer());

    private RecordingCallback callback;
    private Quiz quiz;
    private User user;

    @Before
    public void setUp() {
        callback = new RecordingCallback();
        quiz = new Quiz(QUESTION, OPTIONS, CORRECT);
        user = new User(UUID.randomUUID(), User.Role.Member, "tester", "pass");
        PostDAO.getInstance().clear();
    }

    private MemeCoachManager manager(FeedbackService feedbackService, Executor work, Executor callbacks) {
        return new MemeCoachManager(feedbackService, explanations, this::resolvePersona, work, callbacks);
    }

    private MemeCoachManager manager() {
        return manager(service, DIRECT, DIRECT);
    }

    private FeedbackPersona resolvePersona(String id) {
        if ("penguin".equals(id)) {
            return penguin;
        }
        throw new IllegalArgumentException("Unknown persona: " + id);
    }

    @Test
    public void correctAnswerCallsOnCorrectAndRecordsAnswer() {
        manager().submitAnswer(quiz, user, CORRECT, "penguin", callback);

        assertEquals(List.of("correct"), callback.events);
        assertEquals(Integer.valueOf(CORRECT), quiz.getResponses().get(user.id()));
    }

    @Test
    public void wrongAnswerReturnsFeedbackForThatQuestion() {
        manager().submitAnswer(quiz, user, WRONG, "penguin", callback);

        assertEquals(List.of("feedback"), callback.events);
        QuizFeedback feedback = callback.feedback;
        assertEquals("penguin", feedback.personaId());
        assertEquals("Penguin", feedback.personaName());
        assertEquals("meme_penguin", feedback.imageKey());
        assertEquals(QUESTION, feedback.question());
        assertEquals("Observer", feedback.selectedAnswer());
        assertEquals("Facade", feedback.correctAnswer());
        assertEquals(EXPLANATION, feedback.explanation());
        assertEquals(Integer.valueOf(WRONG), quiz.getResponses().get(user.id()));
    }

    @Test
    public void missingExplanationBecomesEmptyString() {
        Quiz unknown = new Quiz("A question without a stored explanation", OPTIONS, CORRECT);

        manager().submitAnswer(unknown, user, WRONG, "penguin", callback);

        assertEquals("", callback.feedback.explanation());
    }

    @Test
    public void repeatedAnswerIsRejectedAndFirstAnswerKept() {
        MemeCoachManager manager = manager();
        manager.submitAnswer(quiz, user, CORRECT, "penguin", callback);
        manager.submitAnswer(quiz, user, WRONG, "penguin", callback);

        assertEquals(List.of("correct", "error"), callback.events);
        assertEquals(Integer.valueOf(CORRECT), quiz.getResponses().get(user.id()));
    }

    @Test
    public void invalidOptionIsRejectedWithoutRecording() {
        manager().submitAnswer(quiz, user, -1, "penguin", callback);
        manager().submitAnswer(quiz, user, OPTIONS.length, "penguin", callback);

        assertEquals(List.of("error", "error"), callback.events);
        assertTrue(quiz.getResponses().isEmpty());
    }

    @Test
    public void invalidQuizOrUserIsRejected() {
        User withoutId = new User(null, User.Role.Member, "no-id", "pass");

        manager().submitAnswer(null, user, CORRECT, "penguin", callback);
        manager().submitAnswer(quiz, null, CORRECT, "penguin", callback);
        manager().submitAnswer(quiz, withoutId, CORRECT, "penguin", callback);

        assertEquals(List.of("error", "error", "error"), callback.events);
        assertTrue(quiz.getResponses().isEmpty());
    }

    @Test
    public void unknownPersonaIsRejectedWithoutRecording() {
        manager().submitAnswer(quiz, user, WRONG, "dragon", callback);

        assertEquals(List.of("error"), callback.events);
        assertTrue(quiz.getResponses().isEmpty());
    }

    @Test
    public void nullCallbackThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> manager().submitAnswer(quiz, user, CORRECT, "penguin", null));
    }

    @Test
    public void nullDependencyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new MemeCoachManager(null, explanations, this::resolvePersona, DIRECT, DIRECT));
    }

    @Test
    public void serviceFailureIsReportedAsError() {
        FeedbackService failing = (request, persona) -> {
            throw new IllegalStateException("offline");
        };

        manager(failing, DIRECT, DIRECT).submitAnswer(quiz, user, WRONG, "penguin", callback);

        assertEquals(List.of("error"), callback.events);
        assertTrue(callback.errorMessage.contains("offline"));
    }

    @Test
    public void feedbackIsGeneratedOnWorkExecutorAndDeliveredOnCallbackExecutor() {
        QueuedExecutor work = new QueuedExecutor();
        QueuedExecutor callbacks = new QueuedExecutor();

        manager(service, work, callbacks).submitAnswer(quiz, user, WRONG, "penguin", callback);

        // The answer is recorded straight away on the calling thread.
        assertEquals(Integer.valueOf(WRONG), quiz.getResponses().get(user.id()));
        assertEquals(1, work.size());
        assertEquals(0, callbacks.size());
        assertTrue(callback.events.isEmpty());

        work.runAll();
        assertEquals(1, callbacks.size());
        assertTrue(callback.events.isEmpty());

        callbacks.runAll();
        assertEquals(List.of("feedback"), callback.events);
    }

    @Test
    public void correctAndErrorCallbacksUseCallbackExecutor() {
        QueuedExecutor work = new QueuedExecutor();
        QueuedExecutor callbacks = new QueuedExecutor();
        MemeCoachManager manager = manager(service, work, callbacks);

        manager.submitAnswer(quiz, user, CORRECT, "penguin", callback);
        manager.submitAnswer(quiz, user, CORRECT, "penguin", callback);

        assertEquals(0, work.size());
        assertTrue(callback.events.isEmpty());
        callbacks.runAll();
        assertEquals(List.of("correct", "error"), callback.events);
    }

    @Test
    public void makePostUsesPersonaTopicAndShareText() {
        QuizFeedback feedback = sampleFeedback();

        Post post = FeedbackPostFactory.makePost(user, feedback, 1234L);

        assertEquals("Meme Coach Feedback: Penguin", post.topic);
        assertEquals(user.id(), post.poster);
        Iterator<Message> messages = post.messages.getAll();
        Message first = messages.next();
        assertFalse(messages.hasNext());
        assertEquals(feedback.shareText(), first.message());
        assertEquals(user.id(), first.poster());
        assertEquals(post.id, first.thread());
        assertEquals(1234L, first.timestamp());
    }

    @Test
    public void makePostRejectsInvalidInput() {
        User withoutId = new User(null, User.Role.Member, "no-id", "pass");

        assertThrows(IllegalArgumentException.class, () -> FeedbackPostFactory.makePost(null, sampleFeedback()));
        assertThrows(IllegalArgumentException.class, () -> FeedbackPostFactory.makePost(withoutId, sampleFeedback()));
        assertThrows(IllegalArgumentException.class, () -> FeedbackPostFactory.makePost(user, null));
    }

    @Test
    public void shareFeedbackAddsPostToDAO() {
        Post post = manager().shareFeedback(user, sampleFeedback());

        assertSame(post, PostDAO.getInstance().get(new Post(post.id)));
    }

    @Test
    public void shareFeedbackRejectsInvalidInputWithoutAddingPost() {
        assertThrows(IllegalArgumentException.class, () -> manager().shareFeedback(user, null));

        assertFalse(PostDAO.getInstance().getAll().hasNext());
    }

    private QuizFeedback sampleFeedback() {
        return new QuizFeedback("penguin", "Penguin", "Noot noot.", EXPLANATION, "meme_penguin",
                QUESTION, "Observer", "Facade");
    }

    /** Persona with a fixed headline, so tests do not depend on person 1's personas. */
    private record StubPersona(String id, String name, String imageKey) implements FeedbackPersona {
        @Override
        public String getPersonaId() {
            return id;
        }

        @Override
        public String getPersonaName() {
            return name;
        }

        @Override
        public String getImageKey() {
            return imageKey;
        }

        @Override
        public String getSystemPromptStyle() {
            return "";
        }

        @Override
        public String makeHeadline(FeedbackRequest request, Random random) {
            return name + " says: not quite!";
        }
    }

    /** Records which callback methods were called, in order. */
    private static class RecordingCallback implements FeedbackCallback {
        private final List<String> events = new ArrayList<>();
        private QuizFeedback feedback;
        private String errorMessage;

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
            this.errorMessage = message;
        }
    }

    /** Executor that holds tasks until runAll is called, to observe which thread does what. */
    private static class QueuedExecutor implements Executor {
        private final Queue<Runnable> tasks = new ArrayDeque<>();

        @Override
        public void execute(Runnable task) {
            tasks.add(task);
        }

        int size() {
            return tasks.size();
        }

        void runAll() {
            while (!tasks.isEmpty()) {
                tasks.poll().run();
            }
        }
    }
}
