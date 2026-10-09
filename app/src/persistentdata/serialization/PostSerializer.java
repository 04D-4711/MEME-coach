package persistentdata.serialization;
import attachments.Attachment;
import attachments.AttachmentStore;
import dao.model.Post;

import java.util.UUID;

/**
 * Converts between Posts and String[] by converting each field of Post
 * (UUID, poster, topic, and attachment id) to a string, which becomes one of the entries
 * within the array; a post without an attachment stores an empty string
 */
public class PostSerializer implements Serializer<Post, String[]> {
	private static final String NO_ATTACHMENT = "";

	@Override
	public String[] serialize(Post object) {
		Attachment attachment = object.getAttachment();
		String attachmentId = attachment == null ? NO_ATTACHMENT : attachment.getUUID().toString();
		return new String[] {object.id.toString(), object.poster.toString(), object.topic, attachmentId};
	}

	@Override
	public Post deserialize(String[] data) {
		Post post = new Post(UUID.fromString(data[0]), UUID.fromString(data[1]), data[2]);
		if (!data[3].equals(NO_ATTACHMENT)) post.setAttachment(AttachmentStore.load(UUID.fromString(data[3])));
		return post;
	}
}