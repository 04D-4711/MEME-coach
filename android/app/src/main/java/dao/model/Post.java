package dao.model;

import attachments.Attachment;
import dao.MessageComparator;
import sorteddata.SortedData;
import sorteddata.SortedDataFactory;

import java.util.UUID;

public class Post implements HasUUID {
	public final UUID id;
	public final UUID poster;
	public final String topic;
    public final SortedData<Message> messages;
	private Attachment attachment;

	public Post(UUID id, UUID poster, String topic) {
		this.id = id;
		this.poster = poster;
		this.topic = topic;
		this.messages = SortedDataFactory.makeSortedData(MessageComparator.getInstance());
	}

	public Post(UUID id) {
		this(id, null, null);
	}
	
	/** Gives the attachment of this post, or null if it has none. */
	public Attachment getAttachment() {
		return attachment;
	}

	/** Sets or replaces the attachment of this post; null removes it. */
	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public UUID getUUID() { return id; }
}