package attachments;

import dao.model.User;

class Quiz extends Poll{
    private final int correctAnswer;

    public Quiz(String questionText, String[] options, int correctAnswer) {
        super(questionText, options);
        //check if valid for correctanswers
        if (correctAnswer < 0 || correctAnswer >= getOptionCount()) {
            throw new IllegalArgumentException("Invalid correct answer");
        }

        this.correctAnswer = correctAnswer;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    @Override
    public int getInteractionCorrect(User user) {
        if (user == null || user.id() == null) {
            return 0;
        }
        //check useranswer
        Integer answer = getResponses().get(user.id());
        if (answer != null && answer == correctAnswer) {
            return 1;
        }
        return 0;
    }

    void makeSelection(User user, int optionIndex) {
        // TODO: task 3

    }

    float getResult(int optionIndex) {
        // TODO: task 3
        return -1;
    }
}