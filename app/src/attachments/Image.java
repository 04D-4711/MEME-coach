package attachments;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

class Image {
    public Image(java.awt.Image source, String caption) {
        // TODO: task 1
        this(UUID.randomUUID(), Objects.requireNonNull(caption, "Caption must not be null"),
            copy(Objects.requireNonNull(source, "Image source must not be null")));
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