package memecoach;

import java.util.Objects;
import java.util.Random;

/**
 * @version 1.0
 * Chill teacher setting
 *
 * implement FeedbackPersona interface,
 * friendly and encouraging persona
 */

public class ChillTeacherPersona implements FeedbackPersona {

    //*** EXPAND ***
    // expand here if you need more sentence example
    private static final String[] QUOTES = {
            "A wrong answer is just another step toward understanding.",
            "No stress. Let's learn this one together."
    };

    @Override
    public String getPersonaId() {
        //Return the chillteacher ID defined in the factory
        return FeedbackPersonaFactory.TEACHER;
    }

    @Override
    public String getPersonaName() {
        // return the character name displayed on the Android interface
        return "Chill Teacher";
    }

    @Override
    public String getImageKey() {
        //Returns the resource identifier corresponding to the Meme image.
        //The image is loaded by the Android side based on this name.
        return "meme_teacher";
    }

    @Override
    public String getSystemPromptStyle() {
        //Descriptions of the character design for future integration with LLM
        // The current local script version do not require
        return "You are a calm, friendly teacher. "
                + "Use short, clear and reassuring explanations.";
    }

    @Override
    public String makeHeadline(FeedbackRequest request, Random random) {
        //check paras , avoid NullPointerException
        Objects.requireNonNull(request, "Request cannot be null");
        Objects.requireNonNull(random, "Random cannot be null");

        //ramdomly choose a script
        int index = random.nextInt(QUOTES.length);
        //return the chosen script
        return QUOTES[index];
    }
}

