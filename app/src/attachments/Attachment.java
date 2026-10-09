package attachments;

import dao.model.HasUUID;
import dao.model.User;

public abstract class Attachment implements HasUUID {
    // Note for task 5: if you can deduce what this function should do,
    // you may want to implement it to improve the code quality of its caller.
    public static final int CORRECT = 1;
    
    public static final int NOT_CORRECT = 0;

    // please implement this oops sorry
    public int getInteractionCorrect(User user) {
        return NOT_CORRECT;
    }