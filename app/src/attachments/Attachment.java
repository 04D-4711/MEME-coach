package attachments;

import dao.model.HasUUID;
import dao.model.User;

public abstract class Attachment implements HasUUID {
    public static final int CORRECT = 1;
    public static final int NOT_CORRECT = 0;
    public static final int NOT_APPLICABLE = -1;

    public int getInteractionCorrect(User user) {
        return NOT_APPLICABLE;
    }
}
