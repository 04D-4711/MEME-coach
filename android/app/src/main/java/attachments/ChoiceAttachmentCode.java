package attachments;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * @author Jie Ren
 * @version 1.0
 */
public class ChoiceAttachmentCode {
    public ChoiceAttachmentCode() {

    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }
    //read  String.
    private static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length<0 || length >20_000_000) {
            throw new IOException("Invalid string length");
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }
    public static String encode(Poll poll) {
    //let poll or quiz to String
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            //save problem
            writeString(out, poll.getQuestionText());
            //saveoptions
            String[] options = poll.getOptions();
            out.writeInt(options.length);

            for (String option :options) {
                writeString(out, option);
            }

            //save right answer  quiz
            if (poll instanceof Quiz quiz) {
                out.writeInt(quiz.getCorrectAnswer());
            } else {
                out.writeInt(-1);
            }
            //save p
            Map<UUID,Integer> responses = poll.getResponses();
            out.writeInt(responses.size());

            for (Map.Entry<UUID, Integer> entry : responses.entrySet()) {
                UUID userId = entry.getKey();
                out.writeLong(userId.getMostSignificantBits());
                out.writeLong(userId.getLeastSignificantBits());
                out.writeInt(entry.getValue());
            }
            out.flush();
            //to Base64
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to encode attachment", e);
        }
    }

    // 从保存的字符串恢复Poll或Quiz
    public static Poll decode(String encoded) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            DataInputStream in = new DataInputStream(
                    new ByteArrayInputStream(bytes));
            //readquestion
            String question = readString(in);
            //
            int optionCount = in.readInt();
            if (optionCount < 2||optionCount > 1_000_000) {
                throw new IOException("Invalid option count");
            }
            String[] options = new String[optionCount];
            for (int i = 0; i < optionCount; i++) {
                options[i] = readString(in);
            }
            //read right answer read file
            int answer =in.readInt();

            Poll poll =(Poll) new ChoiceAttachmentFactory()
                    .makeAttachment(question, options, answer);
            //
            int responseCount = in.readInt();

            if (responseCount <0|| responseCount > 1_000_000) {
                throw new IOException("Invalid response count");
            }
            //choices
            for (int i = 0; i<responseCount;i++) {
                UUID userId = new UUID(
                        in.readLong(),
                        in.readLong());

                int selectedOption = in.readInt();
                poll.restoreResponse(userId, selectedOption);
            }

            if (in.read()!= -1) {
                throw new IOException("Trailing data");
            }
            return poll;
        } catch (IOException| IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid saved attachment", e);
        }
    }
}
