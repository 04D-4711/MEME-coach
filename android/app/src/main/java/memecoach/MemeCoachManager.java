package memecoach;

import attachments.Quiz;
import dao.PostDAO;
import dao.model.Post;
import dao.model.User;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author Jiaqian Wu
 * @version 1.0
 *
 * Facade between the Android UI and the Meme Coach feature.
 *
 * Threading: the Quiz is only read and written on the calling thread.
 * Feedback is generated on the work executor, and every callback is
 * delivered through the callback executor (e.g. the Android main thread).
 */
public class MemeCoachManager {
    private final FeedbackService feedbackService;
    private final QuizExplanationProvider explanationProvider;
    private final Function<String, FeedbackPersona> personaResolver;
    private final Executor workExecutor;
    private final Executor callbackExecutor;

    /**
     * Creates a manager that resolves personas with FeedbackPersonaFactory.
     */
    public MemeCoachManager(FeedbackService feedbackService,
                            QuizExplanationProvider explanationProvider,
                            Executor workExecutor,
                            Executor callbackExecutor) {
        this(feedbackService, explanationProvider, FeedbackPersonaFactory::makePersona,
                workExecutor, callbackExecutor);
    }

    /**
     * Creates a manager with a custom persona resolver.
     *
     * @param personaResolver maps a persona id to a persona; throws IllegalArgumentException for unknown ids
     */
    public MemeCoachManager(FeedbackService feedbackService,
                            QuizExplanationProvider explanationProvider,
                            Function<String, FeedbackPersona> personaResolver,
                            Executor workExecutor,
                            Executor callbackExecutor) {
        if (feedbackService == null || explanationProvider == null || personaResolver == null
                || workExecutor == null || callbackExecutor == null) {
            throw new IllegalArgumentException("Manager dependencies cannot be null");
        }
        this.feedbackService = feedbackService;
        this.explanationProvider = explanationProvider;
        this.personaResolver = personaResolver;
        this.workExecutor = workExecutor;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * Records the user's answer and reports the outcome through the callback.
     * Invalid input is reported through onError and the answer is not recorded.
     *
     * @param quiz the quiz being answered
     * @param user the user answering
     * @param selectedOption index of the chosen option
     * @param personaId id of the persona that gives feedback on a wrong answer
     * @param callback receives exactly one of onCorrect, onFeedback or onError
     */
    public void submitAnswer(Quiz quiz, User user, int selectedOption, String personaId,
                             FeedbackCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        String error = validateAnswer(quiz, user, selectedOption);
        if (error != null) {
            reportError(callback, error);
            return;
        }

        FeedbackPersona persona;
        try {
            persona = personaResolver.apply(personaId);
        } catch (IllegalArgumentException e) {
            reportError(callback, "Unknown persona: " + personaId);
            return;
        }
        if (persona == null) {
            reportError(callback, "Unknown persona: " + personaId);
            return;
        }

        quiz.makeSelection(user, selectedOption);

        int correctOption = quiz.getCorrectAnswer();
        if (selectedOption == correctOption) {
            callbackExecutor.execute(callback::onCorrect);
            return;
        }

        // Copy what the worker needs so the Quiz never leaves this thread.
        String question = quiz.getQuestionText();
        String selectedAnswer = quiz.getOption(selectedOption);
        String correctAnswer = quiz.getOption(correctOption);
        workExecutor.execute(() -> generateFeedback(question, selectedAnswer, correctAnswer, persona, callback));
    }

    /**
     * Turns the feedback into a Post and adds it to PostDAO.
     *
     * @param user the user who shares the feedback
     * @param feedback the feedback card to share
     * @return the Post that was added
     */
    public Post shareFeedback(User user, QuizFeedback feedback) {
        Post post = FeedbackPostFactory.makePost(user, feedback);
        PostDAO.getInstance().add(post);
        return post;
    }

    /** Returns an error message for invalid input, or null if the answer can be recorded. */
    private static String validateAnswer(Quiz quiz, User user, int selectedOption) {
        if (quiz == null) {
            return "Quiz cannot be null";
        }
        if (user == null || user.id() == null) {
            return "User must have an id";
        }
        if (selectedOption < 0 || selectedOption >= quiz.getOptionCount()) {
            return "Invalid option index";
        }
        if (quiz.getResponses().containsKey(user.id())) {
            return "User has already answered this quiz";
        }
        return null;
    }

    private void generateFeedback(String question, String selectedAnswer, String correctAnswer,
                                  FeedbackPersona persona, FeedbackCallback callback) {
        try {
            String explanation = explanationProvider.getExplanation(question);
            FeedbackRequest request = new FeedbackRequest(question, selectedAnswer, correctAnswer, explanation);
            QuizFeedback feedback = feedbackService.generateFeedback(request, persona);
            if (feedback == null) {
                reportError(callback, "Could not generate feedback");
                return;
            }
            callbackExecutor.execute(() -> callback.onFeedback(feedback));
        } catch (RuntimeException e) {
            reportError(callback, "Could not generate feedback: " + e.getMessage());
        }
    }

    private void reportError(FeedbackCallback callback, String message) {
        callbackExecutor.execute(() -> callback.onError(message));
    }
}
