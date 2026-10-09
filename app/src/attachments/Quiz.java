package attachments;

import dao.model.User;

public class Quiz extends Poll {

    private final int correctAnswer;

    public Quiz(String questionText, String[] options, int correctAnswer) {
        super(questionText, options);

        if (correctAnswer < 0 || correctAnswer >= getOptionCount()) {
            throw new IllegalArgumentException("Invalid correct answer");
        }

        this.correctAnswer = correctAnswer;
    }

    @Override
    public void makeSelection(User user, int optionIndex) {

        super.makeSelection(user, optionIndex);
    }

    @Override
    public float getResult(int optionIndex) {

        return super.getResult(optionIndex);
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    @Override
    public int getInteractionCorrect(User user) {
        if (user == null || user.id() == null) {
            return 0;
        }

        Integer answer = getResponses().get(user.id());

        return answer != null && answer == correctAnswer ? 1 : 0;
    }
}





