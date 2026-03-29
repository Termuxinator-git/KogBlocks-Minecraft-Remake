package items;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ItemSpriteBounds {

    public int minX, minY, maxX, maxY;
    public int width, height;

    public ItemSpriteBounds(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));

            int w = img.getWidth();
            int h = img.getHeight();

            minX = w;
            minY = h;
            maxX = 0;
            maxY = 0;

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int argb = img.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;

                    if (alpha > 10) {
                        if (x < minX) minX = x;
                        if (y < minY) minY = y;
                        if (x > maxX) maxX = x;
                        if (y > maxY) maxY = y;
                    }
                }
            }

            width = maxX - minX + 1;
            height = maxY - minY + 1;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read sprite: " + path, e);
        }
    }
}