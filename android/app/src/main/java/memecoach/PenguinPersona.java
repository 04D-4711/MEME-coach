package memecoach;

import java.util.Objects;
import java.util.Random;

/**
 * @version 1.0
 * Nihilistic Penguin setting.
 *
 * implement FeedbackPersona interface
 * a mocking like tone persona
 */

public class PenguinPersona implements FeedbackPersona {

    //*** EXPAND***
    // expand here if you need more sentence example
    private static final String[] QUOTES = {
            "The penguin has seen your answer and chosen the void.",
            "Wrong answer. The penguin quietly walks away."
    };

    @Override
    public String getPersonaId() {
        //Return the penguin ID defined in the factory
        return FeedbackPersonaFactory.PENGUIN;
    }

    @Override
    public String getPersonaName() {
        // return the character name displayed on the Android interface
        return "Nihilistic Penguin";
    }

    @Override
    public String getImageKey() {
        //Returns the resource identifier corresponding to the Meme image.
        //The image is loaded by the Android side based on this name.
        //who do android incharge of the picture
        return "meme_penguin";
    }

    @Override
    public String getSystemPromptStyle() {
        //Descriptions of the character design for future integration with LLM
        // The current local script version do not require
        return "You are a philosophical, nihilistic penguin. "
                + "Give short, absurd and playful feedback.";
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

