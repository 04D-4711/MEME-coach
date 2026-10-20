
package attachments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import persistentdata.PersistentDataException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/** An attachment that shows one picture with a caption, kept on disk. */
public class Image extends Attachment {

    private static File folder;

    private final UUID id;
    private final String caption;
    private final Bitmap picture;

    /**
     * Initialises the image storage directory for this Android application.
     * Call this before loading or creating image attachments.
     */
    public static synchronized void initialiseStorage(Context context) {
        Objects.requireNonNull(context, "Context must not be null");
        folder = new File(context.getApplicationContext().getFilesDir(), "saved/img");
    }

    private static File getFolder() {
        if (folder == null) {
            throw new IllegalStateException(
                    "Image storage has not been initialised. "
                            + "Call Image.initialiseStorage(context) first.");
        }
        return folder;
    }

    /** Makes a new image and saves it to disk once. */
    public Image(Bitmap source, String caption) {
        this(UUID.randomUUID(),
                Objects.requireNonNull(caption, "Caption must not be null"),
                Objects.requireNonNull(source, "Image source must not be null"));
        save();
    }

    /** Makes an image from parts that are already on disk. */
    private Image(UUID id, String caption, Bitmap picture) {
        this.id = id;
        this.caption = caption;
        this.picture = picture;
    }

    /** Loads an image that was saved before, using its id. */
    public static Image load(UUID id) {
        Objects.requireNonNull(id, "Image id must not be null");

        File imageFile = file(id, "png");
        File captionFile = file(id, "txt");

        try (FileInputStream input = new FileInputStream(imageFile);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             new FileInputStream(captionFile),
                             StandardCharsets.UTF_8))) {

            Bitmap picture = BitmapFactory.decodeStream(input);
            if (picture == null) {
                throw new PersistentDataException("Image " + id + " cannot be read");
            }

            StringBuilder caption = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (caption.length() > 0) {
                    caption.append('\n');
                }
                caption.append(line);
            }

            return new Image(id, caption.toString(), picture);
        } catch (IOException e) {
            throw new PersistentDataException(
                    "Image " + id + " cannot be loaded: " + e.getMessage());
        }
    }

    /** Tells if an image with this id is saved on disk. */
    public static boolean isSaved(UUID id) {
        return file(id, "png").isFile() && file(id, "txt").isFile();
    }

    @Override
    public UUID getUUID() {
        return id;
    }

    /** Gives the caption of this image. */
    public String getCaption() {
        return caption;
    }

    /** Gives the Android bitmap of this image. */
    public Bitmap getPicture() {
        return picture;
    }

    /** Writes the picture and caption into new files named by the id. */

    /** Writes the picture and caption into new files named by the id. */
    private void save() {
        File directory = getFolder();

        if (!directory.exists() && !directory.mkdirs()) {
            throw new PersistentDataException(
                    "Image directory cannot be created: " + directory);
        }

        File imageFile = file(id, "png");
        File captionFile = file(id, "txt");

        if (imageFile.exists() || captionFile.exists()) {
            throw new PersistentDataException("Image " + id + " already exists");
        }

        boolean saved = false;

        try {
            try (FileOutputStream output = new FileOutputStream(imageFile)) {
                if (!picture.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw new IOException("Image cannot be encoded as PNG");
                }
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(captionFile),
                            StandardCharsets.UTF_8))) {
                writer.write(caption);
            }

            saved = true;
        } catch (IOException e) {
            throw new PersistentDataException(
                    "Image " + id + " cannot be saved: " + e.getMessage());
        } finally {
            if (!saved) {
                imageFile.delete();
                captionFile.delete();
            }
        }
    }


    private static File file(UUID id, String ending) {
        return new File(getFolder(), id + "." + ending);
    }

    /**
     * Network loading is intentionally not performed here.
     * Load an image asynchronously, then pass its Bitmap to the constructor.
     */
    public static Image makeExampleImage() {
        throw new UnsupportedOperationException(
                "Download the example image asynchronously on Android, "
                        + "then construct Image with the resulting Bitmap.");
    }
}
