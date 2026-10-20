package attachments;

import dao.PostDAO;
import dao.UserDAO;
import dao.model.Post;
import dao.model.User;

import java.util.HashMap;
import java.util.Iterator;

/** Calculates each user's quiz score across all posts with quiz attachments. */
public class QuizMarker {
        public HashMap<User, Float> execute() {
                HashMap<User, Float> scores = new HashMap<>();

                Iterator<User> users = UserDAO.getInstance().getAll();
                while (users.hasNext()) {
                        User user = users.next();
                        scores.put(user, calculateScore(user));
                }

                return scores;
        }

        private float calculateScore(User user) {
                int attemptedQuizzes = 0;
                int correctAnswers = 0;

                Iterator<Post> posts = PostDAO.getInstance().getAll();
                while (posts.hasNext()) {
                        Attachment attachment = posts.next().getAttachment();
                        if (attachment == null) {
                                continue;
                        }

                        int result = attachment.getInteractionCorrect(user);
                        if (result == Attachment.NOT_APPLICABLE) {
                                continue;
                        }

                        attemptedQuizzes++;
                        if (result == Attachment.CORRECT) {
                                correctAnswers++;
                        }
                }

                return attemptedQuizzes == 0 ? 0f : (float) correctAnswers / attemptedQuizzes;
        }
}
