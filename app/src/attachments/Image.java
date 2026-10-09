package attachments;

import persistentdata.PersistentDataException;
import persistentdata.io.ComputerIOFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/** An attachment that shows one picture with a caption, kept on disk. */
public class Image extends Attachment {
    private static final Path FOLDER = Path.of(ComputerIOFactory.FOLDER, "img");

    private final UUID id;
    private final String caption;
    private final BufferedImage picture;

    /** Makes a new image and saves it to disk once. */
    public Image(java.awt.Image source, String caption) {
        this(UUID.randomUUID(), Objects.requireNonNull(caption, "Caption must not be null"),
                copy(Objects.requireNonNull(source, "Image source must not be null")));
        save();
    }

    /** Makes an image from parts that are already on disk. */
    private Image(UUID id, String caption, BufferedImage picture) {
        this.id = id;
        this.caption = caption;
        this.picture = picture;
    }

    /** Loads an image that was saved before, using its id. */
    public static Image load(UUID id) {
        try {
            BufferedImage picture = ImageIO.read(file(id, "png").toFile());
            if (picture == null) throw new PersistentDataException("Image " + id + " cannot be read");
            return new Image(id, Files.readString(file(id, "txt")), picture);
        } catch (IOException e) {
            throw new PersistentDataException("Image " + id + " cannot be loaded: " + e.getMessage());
        }
    }

    /** Tells if an image with this id is saved on disk. */
    public static boolean isSaved(UUID id) {
        return Files.exists(file(id, "png")) && Files.exists(file(id, "txt"));
    }

    /** Gives the id that names this image on disk. */
    @Override
    public UUID getUUID() {
        return id;
    }

    /** Gives the caption of this image. */
    public String getCaption() {
        return caption;
    }

    /** Gives the picture of this image. */
    public BufferedImage getPicture() {
        return picture;
    }

    /** Writes the picture and caption into new files named by the id. */
    private void save() {
        try {
            Files.createDirectories(FOLDER);
            if (Files.exists(file(id, "png"))) throw new PersistentDataException("Image " + id + " already exists");
            if (!ImageIO.write(picture, "png", file(id, "png").toFile())) {
                throw new PersistentDataException("Image " + id + " has no png writer");
            }
            Files.writeString(file(id, "txt"), caption);
        } catch (IOException e) {
            throw new PersistentDataException("Image " + id + " cannot be saved: " + e.getMessage());
        }
    }

    /** Gives the file for this id with the given ending. */
    private static Path file(UUID id, String ending) {
        return FOLDER.resolve(id + "." + ending);
    }

    /** Turns any picture into a BufferedImage so it can be written. */
    private static BufferedImage copy(java.awt.Image source) {
        if (source instanceof BufferedImage picture) return picture;
        BufferedImage picture = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        picture.getGraphics().drawImage(source, 0, 0, null);
        return picture;
    }

    private static final String IMAGE_SOURCE = "https://images4.alphacoders.com/865/thumb-1920-86546.jpg";

    /**
     * Here's a method that returns an Image attachment.
     * You might use this to ensure your persistence of images works correctly.
     */
    public static Image makeExampleImage() {
        try {
            return new Image(ImageIO.read(new URL(IMAGE_SOURCE)), "Cute kitten");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}