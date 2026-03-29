package render;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;

import inventory.Hotbar;

public class FontRenderer {

    private Font font;
    private Map<String, TextTexture> cache = new HashMap<>();

    private static class TextTexture {
        int id;
        int width;
        int height;

        TextTexture(int id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }
    }

    public FontRenderer() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("Minecraft Seven_2.ttf"))
                    .deriveFont(Font.PLAIN, 16f);
        } catch (Exception e) {
            e.printStackTrace();
            font = new Font("Arial", Font.BOLD, 18);
        }
    }

    public void drawText(Hotbar hotbar, String text, float x, float y) {
        if (text == null || text.isEmpty()) return;

        TextTexture tex = cache.get(text);
        if (tex == null) {
            tex = createTexture(text);
            cache.put(text, tex);
        }

        glBindTexture(GL_TEXTURE_2D, tex.id);
        hotbar.drawTextQuad(x, y, tex.width, tex.height);
    }

    private TextTexture createTexture(String text) {
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        int width = Math.max(1, metrics.stringWidth(text));
        int height = Math.max(1, metrics.getHeight());
        g.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();

        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(text, 0, metrics.getAscent());
        g.dispose();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];

                buffer.put((byte)((pixel >> 16) & 0xFF));
                buffer.put((byte)((pixel >> 8) & 0xFF));
                buffer.put((byte)(pixel & 0xFF));
                buffer.put((byte)((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();

        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                     GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        return new TextTexture(texId, width, height);
    }
    
    public void drawText(TextQuadRenderer quadRenderer, String text, float x, float y) {
        if (text == null || text.isEmpty()) return;

        TextTexture tex = cache.get(text);
        if (tex == null) {
            tex = createTexture(text);
            cache.put(text, tex);
        }

        glBindTexture(GL_TEXTURE_2D, tex.id);
        quadRenderer.drawTextQuad(x, y, tex.width, tex.height);
    }
    
    public int getTextWidth(String text) {
        if (text == null || text.isEmpty()) return 0;

        TextTexture tex = cache.get(text);
        if (tex == null) {
            tex = createTexture(text);
            cache.put(text, tex);
        }

        return tex.width;
    }
    
}