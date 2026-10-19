package memecoach;

/**
 * @author Jiaqian Wu & Jie Ren
 * @version 1.0
 *
 * The Manager is responsible for scheduling threads
 * this interface does not create threads by itself
 *
 */

//*** The callback protocol of MemeCoachManager and the Android UI.
// the Manager is responsible for scheduling threads
// this interface does not create threads by itself
public interface FeedbackCallback {
    //if question answer right, no need meme function
    void onCorrect();

    //if quesiton answer wrong, return a meme fdback
    void onFeedback(QuizFeedback feedback);

    //invalid input or other wrong input
    void onError(String message);
}
