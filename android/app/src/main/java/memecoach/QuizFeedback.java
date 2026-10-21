package memecoach;

/**
 * @author Jiaqian Wu & Jie Ren
 * @version 1.0
 */
public record QuizFeedback(
        String personaId,
        String personaName,
        String headline,
        String explanation,
        String imageKey,
        String question,
        String selectedAnswer,
        String correctAnswer
) {

    //if share the question and answer and coach info, use this format:↓
    public String shareText() {
        return "Meme Coach - " + personaName + "\n"
                + headline + "\n\n"
                + "Question: " + question + "\n"
                + "My answer: " + selectedAnswer + "\n"
                + explanation + "\nCorrect answer: " + correctAnswer + "\n";
    }
}

