package persistentdata.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ComputerIOFactory implements IOFactory {
	/** The folder, relative to where the program runs, that holds all saved data. */
	public static final String FOLDER = "saved";
	private static final String FULL_FILENAME_TEMPLATE = FOLDER + "/%s.txt";
	private static String parseFullFilename(String file) {
		return FULL_FILENAME_TEMPLATE.formatted(file);
	}

	@Override
	public Writer writer(String filename) {
		try {
			Files.createDirectories(Path.of(FOLDER));
			return new FileWriter(parseFullFilename(filename));
		} catch (IOException ignored) {
			return null;
		}
	}

	@Override
	public Reader reader(String filename) {
		try {
			return new FileReader(parseFullFilename(filename));
		} catch (IOException ignored) {
			return null;
		}
	}
}