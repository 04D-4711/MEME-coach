package memecoach;

/**
 * @author Jiaqian Wu & Jie Ren
 * @version 1.0
 */
public record FeedbackRequest(
        String question,
        String selectedAnswer,
        String correctAnswer,
        String correctExplanation
) {
    //Check required fields
    //allow null or empty string when no default explanation is provided.
    public FeedbackRequest {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        if (selectedAnswer == null || selectedAnswer.isBlank()) {
            throw new IllegalArgumentException("Selected answer cannot be empty");
        }
        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new IllegalArgumentException("Correct answer cannot be empty");
        }
        if (correctExplanation == null) {
            correctExplanation = "";
        }
    }
}
