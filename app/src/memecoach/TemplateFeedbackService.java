package memecoach;

import java.util.Random;

/**
 * @version 1.0
 *
 * Local, template-based FeedbackService used by the app (no network).
 *
 * TODO(person 1): skeleton only, see MEMECOACH_PERSON1_TODO.md.
 */
public class TemplateFeedbackService implements FeedbackService {
    private final Random random;

    public TemplateFeedbackService(Random random) {
        this.random = random;
    }

    @Override
    public QuizFeedback generateFeedback(FeedbackRequest request, FeedbackPersona persona) {
        throw new UnsupportedOperationException("TODO(person 1): TemplateFeedbackService.generateFeedback");
    }
}
