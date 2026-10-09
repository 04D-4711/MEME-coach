package attachments;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/** An attachment that shows one picture with a caption. */
public class Image extends Attachment {
    private final UUID id;
    private final String caption;
    private final BufferedImage picture;

    /** Makes a new image with a new id. */
    public Image(java.awt.Image source, String caption) {
        this(UUID.randomUUID(), Objects.requireNonNull(caption, "Caption must not be null"),
                copy(Objects.requireNonNull(source, "Image source must not be null")));
    }

    /** Makes an image from its id, caption and picture. */
    private Image(UUID id, String caption, BufferedImage picture) {
        this.id = id;
        this.caption = caption;
        this.picture = picture;
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