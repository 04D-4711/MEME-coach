package memecoach;

/**
 * @version 1.0
 *
 * Factory that creates a persona from its id ("supportive", "penguin", "teacher").
 *
 * TODO(person 1): skeleton only, see MEMECOACH_PERSON1_TODO.md.
 */
public final class FeedbackPersonaFactory {
    private FeedbackPersonaFactory() {}

    /**
     * Creates the persona with the given id.
     *
     * @param id the persona id
     * @return the persona
     * @throws IllegalArgumentException if the id is unknown
     */
    public static FeedbackPersona makePersona(String id) {
        throw new UnsupportedOperationException("TODO(person 1): FeedbackPersonaFactory.makePersona");
    }
}
