package memecoach;

import java.util.Objects;
import java.util.Random;

/**
 * @version 1.0
 * Supportive persona setting
 *
 * Implement FeedbackPersona interface, is the detailed strategy for strategy pattern
 * Responsible for proviing the name, image resource and local talk sentence
 */

public class SupportiveDisappointedPersona implements FeedbackPersona {

    //*** EXPAND ***
    // expand here if you need more sentence example
    private static final String[] QUOTES = {
            "I'm not angry. Just disappointed.",
            "That was a bold choice. Unfortunately, wrong."
    };

    @Override
    public String getPersonaId() {
        //Return the character ID defined in the factory
        return FeedbackPersonaFactory.SUPPORTIVE;
    }

    @Override
    public String getPersonaName() {
        //JR: who do Android pay attention to this
        // return the character name displayed on the Android interface
        return "Supportive Disappointed";
    }

    @Override
    public String getImageKey() {
        //Returns the resource identifier corresponding to the Meme image.
        //The image is loaded by the Android side based on this name.
        return "meme_supportive";
    }

    @Override
    public String getSystemPromptStyle() {
        //Descriptions of the character design for future integration with LLM
        // The current local script version do not require
        //but still put it here if we need expand
        return "You are a supportive but disappointed teacher. "
                + "Give short, funny and encouraging feedback.";
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

