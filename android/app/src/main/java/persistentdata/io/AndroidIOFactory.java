
package persistentdata.io;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

public class AndroidIOFactory implements IOFactory {

    private final File folder;

    public AndroidIOFactory(Context context) {
        Objects.requireNonNull(context, "Context must not be null");
        this.folder = new File(
                context.getApplicationContext().getFilesDir(), "saved");
    }

    @Override
    public Writer writer(String filename) {
        ensureFolderExists();

        try {
            return new FileWriter(getFile(filename));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot open file for writing: " + filename, e);
        }
    }

    @Override
    public Reader reader(String filename) {
        File file = getFile(filename);

        if (!file.exists()) {
            return null;
        }

        try {
            return new FileReader(file);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot open file for reading: " + filename, e);
        }
    }

    private void ensureFolderExists() {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException(
                    "Cannot create storage directory: " + folder);
        }

        if (!folder.isDirectory()) {
            throw new IllegalStateException(
                    "Storage path is not a directory: " + folder);
        }
    }

    private File getFile(String filename) {
        Objects.requireNonNull(filename, "Filename must not be null");

        if (filename.isBlank()
                || filename.contains("/")
                || filename.contains("\\")
                || filename.equals(".")
                || filename.equals("..")) {
            throw new IllegalArgumentException(
                    "Invalid storage filename: " + filename);
        }

        return new File(folder, filename + ".txt");
    }
}


