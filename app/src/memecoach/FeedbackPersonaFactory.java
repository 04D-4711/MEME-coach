package memecoach;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * @version 1.0
 *
 * Factory that creates a persona from its id ("supportive", "penguin", "teacher").
 *
 * TODO(person 1): skeleton only, see MEMECOACH_PERSON1_TODO.md.
 */
public final class FeedbackPersonaFactory {
    //Unique IDs used to identify the three personas
    public static final String SUPPORTIVE = "supportive";
    public static final String PENGUIN = "penguin";
    public static final String TEACHER = "teacher";

    private FeedbackPersonaFactory() {}

    /**
     * Creates the persona with the given id.
     *
     * @param id the persona id
     * @return the persona
     * @throws IllegalArgumentException if the id is unknown
     */
    public static FeedbackPersona makePersona(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Persona ID cannot be null");
        }

        return switch (id) {
            case SUPPORTIVE -> new SupportiveDisappointedPersona();
            case PENGUIN -> new PenguinPersona();
            case TEACHER -> new ChillTeacherPersona();

            default -> throw new IllegalArgumentException(
                    "Unknown persona: " + id
            );
        };
    }

}
