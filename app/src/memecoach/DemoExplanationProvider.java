package memecoach;

import java.util.Map;

/**
 * @version 1.0
 *
 * QuizExplanationProvider backed by a fixed map from question text to explanation.
 *
 * TODO(person 1): skeleton only, see MEMECOACH_PERSON1_TODO.md.
 */
public class DemoExplanationProvider implements QuizExplanationProvider {

    public DemoExplanationProvider(Map<String, String> explanations) {
        throw new UnsupportedOperationException("TODO(person 1): DemoExplanationProvider constructor");
    }

    @Override
    public String getExplanation(String question) {
        throw new UnsupportedOperationException("TODO(person 1): DemoExplanationProvider.getExplanation");
    }
}
