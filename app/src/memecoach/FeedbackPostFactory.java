package memecoach;

import dao.model.Message;
import dao.model.Post;
import dao.model.User;

import java.util.UUID;

/**
 * @author Jiaqian Wu
 * @version 1.0
 *
 * Factory that turns a meme feedback card into a Post that can be shared.
 * It only builds the Post; adding it to PostDAO is the caller's job.
 */
public final class FeedbackPostFactory {
    private static final String TOPIC_PREFIX = "Meme Coach Feedback: ";

    private FeedbackPostFactory() {}

    /**
     * Builds a Post for the feedback, timestamped with the current time.
     *
     * @param user the user who shares the feedback
     * @param feedback the feedback card to share
     * @return a new Post whose first message is the feedback's share text
     */
    public static Post makePost(User user, QuizFeedback feedback) {
        return makePost(user, feedback, System.currentTimeMillis());
    }

    /**
     * Builds a Post for the feedback with a given timestamp.
     *
     * @param user the user who shares the feedback
     * @param feedback the feedback card to share
     * @param timestamp the UNIX time in milliseconds of the first message
     * @return a new Post whose first message is the feedback's share text
     */
    public static Post makePost(User user, QuizFeedback feedback, long timestamp) {
        if (user == null || user.id() == null) {
            throw new IllegalArgumentException("User must have an id");
        }
        if (feedback == null) {
            throw new IllegalArgumentException("Feedback cannot be null");
        }

        Post post = new Post(UUID.randomUUID(), user.id(), TOPIC_PREFIX + feedback.personaName());
        post.messages.insert(new Message(UUID.randomUUID(), user.id(), post.id, timestamp, feedback.shareText()));
        return post;
    }
}
