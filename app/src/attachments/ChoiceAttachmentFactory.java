package attachments;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

class ChoiceAttachmentFactory {

    public Attachment makeAttachment(String questionText, String[] options, int correctAnswer) {
        // TODO: task 2
        // 至少需要两个选项
        if (options == null || options.length < 2) {
            throw new IllegalArgumentException("At least two options are required");
        }

        // 如果correctAnswer是有效索引，创建Quiz
        if (correctAnswer >= 0 && correctAnswer < options.length) {
            return new Quiz(questionText, options, correctAnswer);
        }

        // 如果correctAnswer无效，创建Poll
        return new Poll(questionText, options);

    }
}