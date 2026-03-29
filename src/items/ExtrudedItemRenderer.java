package items;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ExtrudedItemRenderer {

    private final int width;
    private final int height;
    private final int[][] pixels;

    public ExtrudedItemRenderer(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) {
                throw new RuntimeException("Failed to load item image: " + path);
            }

            width = img.getWidth();
            height = img.getHeight();
            pixels = new int[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixels[x][y] = img.getRGB(x, y);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load item image: " + path, e);
        }
    }

    public void render() {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_CULL_FACE);

        float pixelW = 1.0f / width;
        float pixelH = 1.0f / height;
        float halfDepth = Math.min(pixelW, pixelH) * 0.5f;

        for (int px = 0; px < width; px++) {
            for (int py = 0; py < height; py++) {
                int argb = pixels[px][py];
                float a = ((argb >> 24) & 0xFF) / 255f;

                if (a <= 0.05f) continue;

                float r = ((argb >> 16) & 0xFF) / 255f;
                float g = ((argb >> 8) & 0xFF) / 255f;
                float b = (argb & 0xFF) / 255f;

                float x0 = -0.5f + px * pixelW;
                float x1 = x0 + pixelW;

                float y1 = 0.5f - py * pixelH;
                float y0 = y1 - pixelH;

                float z0 = -halfDepth;
                float z1 = halfDepth;

                glColor4f(r, g, b, a);
                glBegin(GL_QUADS);
                glNormal3f(0f, 0f, 1f);
                glVertex3f(x0, y0, z1);
                glVertex3f(x1, y0, z1);
                glVertex3f(x1, y1, z1);
                glVertex3f(x0, y1, z1);
                glEnd();

                glColor4f(r, g, b, a);
                glBegin(GL_QUADS);
                glNormal3f(0f, 0f, -1f);
                glVertex3f(x1, y0, z0);
                glVertex3f(x0, y0, z0);
                glVertex3f(x0, y1, z0);
                glVertex3f(x1, y1, z0);
                glEnd();

                if (!isOpaque(px - 1, py)) {
                    shade(r, g, b, a, 0.95f);
                    glBegin(GL_QUADS);
                    glNormal3f(-1f, 0f, 0f);
                    glVertex3f(x0, y0, z0);
                    glVertex3f(x0, y0, z1);
                    glVertex3f(x0, y1, z1);
                    glVertex3f(x0, y1, z0);
                    glEnd();
                }

                if (!isOpaque(px + 1, py)) {
                    shade(r, g, b, a, 0.95f);
                    glBegin(GL_QUADS);
                    glNormal3f(1f, 0f, 0f);
                    glVertex3f(x1, y0, z1);
                    glVertex3f(x1, y0, z0);
                    glVertex3f(x1, y1, z0);
                    glVertex3f(x1, y1, z1);
                    glEnd();
                }

                if (!isOpaque(px, py - 1)) {
                    shade(r, g, b, a, 1.00f);
                    glBegin(GL_QUADS);
                    glNormal3f(0f, 1f, 0f);
                    glVertex3f(x0, y1, z1);
                    glVertex3f(x1, y1, z1);
                    glVertex3f(x1, y1, z0);
                    glVertex3f(x0, y1, z0);
                    glEnd();
                }

                if (!isOpaque(px, py + 1)) {
                    shade(r, g, b, a, 0.90f);
                    glBegin(GL_QUADS);
                    glNormal3f(0f, -1f, 0f);
                    glVertex3f(x0, y0, z0);
                    glVertex3f(x1, y0, z0);
                    glVertex3f(x1, y0, z1);
                    glVertex3f(x0, y0, z1);
                    glEnd();
                }
            }
        }

        glColor4f(1f, 1f, 1f, 1f);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
    }

    private boolean isOpaque(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        int argb = pixels[x][y];
        float a = ((argb >> 24) & 0xFF) / 255f;
        return a > 0.05f;
    }

    private void shade(float r, float g, float b, float a, float mul) {
        glColor4f(r * mul, g * mul, b * mul, a);
    }
}