package memecoach;

import java.util.Random;

/**
 * @author Jiaqian Wu & Jie Ren
 * @version 1.0
 */
public interface FeedbackPersona {
    //stable machine recognition IDs, supportive, penguin, teacher.
    String getPersonaId();

    //persona names shows on UI
    String getPersonaName();

    //JR:who does Android pls notice here:The corresponding resource name
    //in the Android drawable, without the .png suffix
    String getImageKey();

    //style description prepared for future integration with LLM(If we still have time...
    //local transcript version is not connected to the internet
    String getSystemPromptStyle();

    //generate a title from the quotes of this character design
    //Random is uniformly input by the service for ease of testing
    String makeHeadline(FeedbackRequest request, Random random);
}