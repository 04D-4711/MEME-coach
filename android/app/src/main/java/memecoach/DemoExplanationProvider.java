package memecoach;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @version 1.0
 * Provides predefined explanations for demo quizzes
 *
 * Implements QuizExplanationProvider using a Map.
 * Each question is associated with an explanation
 *
 * this class does not generate new explanations
 * It only retrieves explanations stored in advance.
 */

public class DemoExplanationProvider implements QuizExplanationProvider {
    //Store predefined quiz explanations
    //Key:question text
    //Value:explanation text
    private final Map<String, String> explanations;

    /**
     * Creates an explanation provider.
     *
     * @param explanations Map containing questions and explanations
     */
    public DemoExplanationProvider(Map<String, String> explanations) {
        //Check that the input map is not null
        Objects.requireNonNull(explanations, "Explanations cannot be null");

        //for safety, copy the map to prevent external modifications
        //from changing the explanations stored here
        this.explanations = new HashMap<>(explanations);
    }

    /**
     * Finds the explanation associated with a question.
     *
     * @param question The exact quiz question text
     * @return Explanation text, or an empty string if not found
     */
    @Override
    public String getExplanation(String question) {
        //return an empty string if the question is null
        if (question == null) {
            return "";
        }

        //Find the explanation using the question text
        String explanation = explanations.get(question);
        //Return an empty string if no explanation exists
        return explanation == null ? "" : explanation;
    }
}
