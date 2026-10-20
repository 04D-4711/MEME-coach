package attachments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Finds a saved attachment again from its id. */
public class AttachmentStore {
    private static final Map<UUID, Attachment> LOADED = new HashMap<>();

    private AttachmentStore() {
    }

    /** Loads the saved attachment with this id once, so posts sharing it share one object; null if none is saved. */
    public static Attachment load(UUID id) {
        if (LOADED.containsKey(id)) return LOADED.get(id);
        if (!Image.isSaved(id)) return null;
        Attachment attachment = Image.load(id);
        LOADED.put(id, attachment);
        return attachment;
    }
}