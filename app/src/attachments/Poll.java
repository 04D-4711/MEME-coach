package attachments;

import dao.model.HasUUID;
import dao.model.User;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Poll extends Attachment implements HasUUID {
    private final String questionText;
    private final String[] options;
    private final Map<UUID, Integer> responses = new LinkedHashMap<>();
    public Poll(String questionText, String[] options) {
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }
        if (options == null || options.length < 2) {
            throw new IllegalArgumentException("At least two options are required");
        }
        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new IllegalArgumentException("Option labels must not be empty");
            }
        }
        this.questionText = questionText;
        this.options = options.clone();
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options.clone();
    }

    public int getOptionCount() {
        return options.length;
    }

    public String getOption(int index) {
        return options[index];
    }

    public Map<UUID, Integer> getResponses() {
        return Collections.unmodifiableMap(responses);
    }

    void makeSelection(User user, int optionIndex) {
        // TODO: task 3
        if (user == null|| user.id() == null) {
            throw new IllegalArgumentException("Invalid user");
        }

        if (optionIndex< 0|| optionIndex >= options.length) {
            throw new IllegalArgumentException("Invalid option index");
        }

        responses.put(user.id(), optionIndex);
    }

    float getResult(int optionIndex) {
        // TODO: task 3


        if (optionIndex <0 ||optionIndex >= options.length) {
            throw new IllegalArgumentException("Invalid option index");
        }

        if (responses.isEmpty()) {
            return 0f;
        }

        int count = 0;

        for (int selected : responses.values()) {
            if (selected == optionIndex) {
                count++;
            }
        }

        return (float) count /responses.size();
    }

    @Override
    public int getInteractionCorrect(User user) {
        return NOT_APPLICABLE;
    }

    public void restoreResponse(UUID userId, int optionIndex) {

        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (optionIndex < 0||optionIndex>= options.length) {
            throw new IllegalArgumentException("Invalid response index");
        }
        responses.put(userId, optionIndex);
    }

    @Override
    public UUID getUUID() {
        //not finished rememberplease
        return null;
    }
}