package persistentdata.serialization;
import attachments.*;
import dao.model.Post;

import java.util.UUID;

/**
 * Converts between Posts and String[] by converting each field of Post
 * (UUID, poster, topic, and attachment id) to a string, which becomes one of the entries
 * within the array; a post without an attachment stores an empty string
 */
public class PostSerializer implements Serializer<Post, String[]> {
	private static final String NO_ATTACHMENT = "";
	private static final String POLL_PREFIX = "POLL:";
	private static final String QUIZ_PREFIX = "QUIZ:";

	@Override
	public String[] serialize(Post object) {
		Attachment attachment = object.getAttachment();
		String attachmentData = NO_ATTACHMENT;

		if (attachment instanceof Quiz quiz) {
			attachmentData = QUIZ_PREFIX + ChoiceAttachmentCode.encode(quiz);

		} else if (attachment instanceof Poll poll) {
			attachmentData = POLL_PREFIX + ChoiceAttachmentCode.encode(poll);

		} else if (attachment!=null) {
			attachmentData = attachment.getUUID().toString();
		}

		return new String[] {
				object.id.toString(),
				object.poster.toString(),
				object.topic,
				attachmentData
		};
	}

	@Override
	public Post deserialize(String[] data) {
		Post post = new Post(
				UUID.fromString(data[0]),
				UUID.fromString(data[1]),
				data[2]
		);

		String attachmentData = data[3];
		if (attachmentData == null || attachmentData.isEmpty()) {
			return post;
		}
		if (attachmentData.startsWith(QUIZ_PREFIX)) {

			Poll attachment = ChoiceAttachmentCode.decode(
					attachmentData.substring(QUIZ_PREFIX.length())
			);
			if (!(attachment instanceof Quiz)) {
				throw new IllegalArgumentException("Expected Quiz");
			}
			post.setAttachment(attachment);
		} else if (attachmentData.startsWith(POLL_PREFIX)) {
			Poll attachment = ChoiceAttachmentCode.decode(
					attachmentData.substring(POLL_PREFIX.length())
			);

			if (attachment instanceof Quiz) {
				throw new IllegalArgumentException("Expected Poll");
			}

			post.setAttachment(attachment);

		} else {
			UUID attachmentId = UUID.fromString(attachmentData);
			post.setAttachment(AttachmentStore.load(attachmentId));
		}
		return post;
	}
}