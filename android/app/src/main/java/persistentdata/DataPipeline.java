package persistentdata;

import dao.model.Post;
import persistentdata.formatted.*;
import persistentdata.io.ComputerIOFactory;
import persistentdata.io.IOFactory;
import persistentdata.serialization.*;
import dao.*;

import java.io.*;
import java.util.Iterator;

public class DataPipeline<T, S> {
	private final IOFactory ioFactory;
	private final FormattedFactory<S> formattedFactory;
	private final Serializer<T, S> serializer;
	private final String filename;

	public DataPipeline(IOFactory ioFactory, FormattedFactory<S> formattedFactory, Serializer<T, S> serializer, String filename) {
		this.ioFactory = ioFactory;
		this.formattedFactory = formattedFactory;
		this.serializer = serializer;
		this.filename = filename;
	}


	private static final UserDAO users = UserDAO.getInstance();
	private static final PostDAO posts = PostDAO.getInstance();

	public void writeFrom(Iterator<T> iterator) {
		try {
			Writer writer = ioFactory.writer(filename);
			if (writer == null) return;
			FormattedWriter<S> formattedWriter = formattedFactory.writer(writer);

			while (iterator.hasNext()) {
				formattedWriter.putNext(serializer.serialize(iterator.next()));
			}

			writer.close();
		} catch (IOException e) {
			throw new PersistentDataException(e.getMessage());
		}
	}

	public interface AddToDAO<T> {
		void run(T item);
	}

	public void readTo(AddToDAO<T> callback) {
		try {
			Reader reader = ioFactory.reader(filename);
			if (reader == null) return;
			FormattedReader<S> formattedReader = formattedFactory.reader(reader);

			while (formattedReader.hasNext()) {
				callback.run(serializer.deserialize(formattedReader.getNext()));
			}

			reader.close();
		} catch (IOException e) {
			throw new PersistentDataException(e.getMessage());
		}
	}
}
//修改一：app/src/persistentdata/DataPipeline.java
//原有问题：
//类中存在一个实例字段 postPipeline，其初始化过程会创建新的 DataPipeline 实例。
//新实例初始化时又会创建另一个 DataPipeline，形成递归式对象创建，可能导致 StackOverflowError。
//修改内容：
//删除 postPipeline 字段及其初始化表达式。
//
//预期效果：
//
//避免 DataPipeline 创建对象时不断递归初始化，为后续正常构建和使用数据管道消除这一障碍。