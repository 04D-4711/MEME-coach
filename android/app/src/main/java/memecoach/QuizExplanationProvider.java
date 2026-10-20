package memecoach;

/**
 * @author Jiaqian Wu & Jie Ren
 * @version 1.0
 */
public interface QuizExplanationProvider {
    //search for the knowledge for explanation of this question by text of this question
    // if no result is found, return an empty string
    String getExplanation(String question);
}
