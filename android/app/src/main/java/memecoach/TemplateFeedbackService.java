package memecoach;

import java.util.Objects;
import java.util.Random;

/**
 * @version 1.0
 *
 * Local, template-based FeedbackService used by the app (no network).
 * Implements FeedbackService to generate Meme Coach feedback
 * using predefined quotes instead of an external LLM API.
 * this class only generates feedback data.
 * Quiz validation and answer submission are handled by MemeCoachManager.
 * 在此组装选择的选项，正确答案，解析，图片，Persona和它对应的quote script与meme
 */

public class TemplateFeedbackService implements FeedbackService {
    //Random generator used by different Persona strategies
    private final Random random;

    /**
     * Creates a local feedback service
     * @param random Random generator for selecting quotes
     */
    public TemplateFeedbackService(Random random) {
        this.random = random;
    }

    /**
     * Generates a complete Meme Coach feedback result.
     * @param request Information about the incorrect quiz answer
     * @param persona Selected feedback persona
     * @return QuizFeedback containing the meme text and image key
     */
    @Override
    public QuizFeedback generateFeedback(FeedbackRequest request, FeedbackPersona persona) {
        //input objects
        Objects.requireNonNull(request, "Request cannot be null");
        Objects.requireNonNull(persona, "Persona cannot be null");

        //Ask the selected Persona to choose a quote
        String headline = persona.makeHeadline(request, random);
        //get the predefined explaination
        String explanation = request.correctExplanation();

        //Use a generic message if no explanation exists
        if (explanation.isBlank()) {
            explanation = "No explanation is available for this question. "
                    + "Review the correct answer and the related concept.";
        }

        //Combine all information into a feedback result
        return new QuizFeedback(
                persona.getPersonaId(),
                persona.getPersonaName(),
                headline,
                explanation,
                persona.getImageKey(),
                request.question(),
                request.selectedAnswer(),
                request.correctAnswer()
        );
    }
}
