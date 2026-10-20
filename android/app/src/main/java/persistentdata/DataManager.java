
package persistentdata;

import android.content.Context;

import dao.PostDAO;
import dao.UserDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import persistentdata.formatted.CSVFormat;
import persistentdata.formatted.CSVFormattedFactory;
import persistentdata.io.AndroidIOFactory;
import persistentdata.io.IOFactory;
import persistentdata.serialization.MessageSerializer;
import persistentdata.serialization.PostSerializer;
import persistentdata.serialization.UserSerializer;

public class DataManager {

	private static DataManager instance;

	public static synchronized DataManager getInstance(Context context) {
		if (instance == null) {
			instance = new DataManager(context.getApplicationContext());
		}
		return instance;
	}

	private final IOFactory IO;

	private final DataPipeline<User, String[]> userPipeline;
	private final DataPipeline<Post, String[]> postPipeline;
	private final DataPipeline<Message, String[]> messagePipeline;

	private final UserDAO users = UserDAO.getInstance();
	private final PostDAO posts = PostDAO.getInstance();

	private DataManager(Context context) {
		this.IO = new AndroidIOFactory(context);

		this.userPipeline = new DataPipeline<>(
				IO, new CSVFormattedFactory(new CSVFormat(4)),
				new UserSerializer(), "users");

		this.postPipeline = new DataPipeline<>(
				IO, new CSVFormattedFactory(new CSVFormat(4)),
				new PostSerializer(), "posts");

		this.messagePipeline = new DataPipeline<>(
				IO, new CSVFormattedFactory(new CSVFormat(5)),
				new MessageSerializer(), "messages");
	}

	public void readAll() {
		users.clear();
		posts.clear();

		userPipeline.readTo(users::add);
		postPipeline.readTo(posts::add);
		messagePipeline.readTo(
				message -> posts.get(new Post(message.thread()))
						.messages.insert(message));
	}

	public void writeAll() {
		userPipeline.writeFrom(users.getAll());
		postPipeline.writeFrom(posts.getAll());
		messagePipeline.writeFrom(posts.getAllMessages());
	}
}
