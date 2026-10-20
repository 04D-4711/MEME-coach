package memecoach;

/**
 * @author Jiaqian Wu & Jie Ren
 * @version 1.0
 */

public interface FeedbackService {
    /**
     * Generate data for a Meme feedback card for the answers is incorrect.
     *
     * @param request Question, User Answer, Correct Answer and Preset Explanation
     * @param persona The current persona strategy
     * @return The unified feedback results for the UI and sharing functions that can be utilized
     */
    QuizFeedback generateFeedback(FeedbackRequest request, FeedbackPersona persona);
}
