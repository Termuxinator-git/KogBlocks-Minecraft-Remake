package inventory;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import core.Input;
import items.BlockItemRenderer;
import items.ItemSpriteBounds;
import items.ItemTypes;
import render.FontRenderer;
import render.TextQuadRenderer;
import render.Texture;
import render.TextureManager;
import world.Chunk;

public class Hotbar implements TextQuadRenderer {
    private int shaderProgram;
    private int vaoId, vboId;
    private Texture hotbarTex;
    private BlockItemRenderer blockRenderer;
    private FontRenderer fontRenderer;
    private Texture selectionTex;

    private int selectedSlot = 0;
    private final Inventory inventory;

    private static final float W = 1024f;
    private static final float H = 768f;

    private static final float HOTBAR_COUNT_OFFSET_X = 13f;
    private static final float HOTBAR_COUNT_OFFSET_Y = 13f;

    private static final float INVENTORY_COUNT_OFFSET_X = 18f;
    private static final float INVENTORY_COUNT_OFFSET_Y = 16f;

    private final ItemSpriteBounds stickBounds =
            new ItemSpriteBounds("textures/items/nature/stick.png");

    private final ItemSpriteBounds woodenPickaxeBounds =
            new ItemSpriteBounds("textures/items/tools/wooden_pickaxe.png");
    private final ItemSpriteBounds woodenShovelBounds =
            new ItemSpriteBounds("textures/items/tools/wooden_shovel.png");
    private final ItemSpriteBounds woodenAxeBounds =
            new ItemSpriteBounds("textures/items/tools/wooden_axe.png");

    public Hotbar(Inventory inventory) {
        this.inventory = inventory;
    }

    public void init() {
        hotbarTex = new Texture("textures/gui/hotbar.png");
        fontRenderer = new FontRenderer();
        selectionTex = new Texture("textures/gui/hotbar_selection.png");

        blockRenderer = new BlockItemRenderer();
        blockRenderer.init();

        String vertSrc =
            "#version 330 core\n" +
            "layout(location = 0) in vec2 aPos;\n" +
            "layout(location = 1) in vec2 aUV;\n" +
            "out vec2 uv;\n" +
            "void main() { uv = aUV; gl_Position = vec4(aPos, 0.0, 1.0); }\n";

        String fragSrc =
            "#version 330 core\n" +
            "in vec2 uv;\n" +
            "out vec4 FragColor;\n" +
            "uniform sampler2D tex;\n" +
            "uniform float alpha;\n" +
            "void main() {\n" +
            "    vec4 c = texture(tex, uv);\n" +
            "    if (uv.x == 0.0 && uv.y == 0.0) {\n" +
            "        FragColor = vec4(0.0, 0.0, 0.0, alpha);\n" +
            "    } else {\n" +
            "        if (c.a < 0.1) discard;\n" +
            "        FragColor = vec4(c.rgb, c.a * alpha);\n" +
            "    }\n" +
            "}\n";

        int vert = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vert, vertSrc);
        glCompileShader(vert);

        int frag = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(frag, fragSrc);
        glCompileShader(frag);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vert);
        glAttachShader(shaderProgram, frag);
        glLinkProgram(shaderProgram);

        glDeleteShader(vert);
        glDeleteShader(frag);

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, new float[24], GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    public void handleInput() {
        for (int i = 0; i < 9; i++) {
            if (Input.isKeyDown(org.lwjgl.glfw.GLFW.GLFW_KEY_1 + i)) {
                selectedSlot = i;
            }
        }

        double scroll = Input.getScrollDelta();
        if (scroll > 0) {
            selectedSlot--;
            if (selectedSlot < 0) selectedSlot = 8;
        } else if (scroll < 0) {
            selectedSlot++;
            if (selectedSlot > 8) selectedSlot = 0;
        }

        Input.resetScroll();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getSelectedItem() {
        return inventory.getHotbarItem(selectedSlot);
    }

    public void removeSelectedItem() {
        inventory.removeFromHotbar(selectedSlot);
    }

    public int getSelectedBlock() {
        return getSelectedItem();
    }

    public void removeSelectedBlock() {
        removeSelectedItem();
    }

    public void addItem(int item) {
        inventory.addItem(item);
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void drawQuad(float x, float y, float w, float h, float u0, float v0, float u1, float v1) {
        float nx0 = (x / W) * 2f - 1f;
        float ny0 = 1f - (y / H) * 2f;
        float nx1 = ((x + w) / W) * 2f - 1f;
        float ny1 = 1f - ((y + h) / H) * 2f;

        float[] data = {
            nx0, ny1,  u0, v1,
            nx1, ny1,  u1, v1,
            nx1, ny0,  u1, v0,
            nx1, ny0,  u1, v0,
            nx0, ny0,  u0, v0,
            nx0, ny1,  u0, v1,
        };

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
    }

    public void render() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(shaderProgram);
        glBindVertexArray(vaoId);

        float barW = 364f;
        float barH = 44f;
        float barX = (W - barW) / 2f;
        float barY = H - barH - 2f;

        hotbarTex.bind();
        glUniform1i(glGetUniformLocation(shaderProgram, "tex"), 0);
        glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), 1f);

        drawQuad(barX, barY, barW, barH, 0f, 0f, 1f, 1f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        float slotSize = 36f;
        float startX = barX + 14f;
        float iconY = barY + 4f;

        float selectedX = startX + selectedSlot * 40f;
        float selectedY = iconY;

        float scale = 1.14f;
        float size = slotSize * scale;

        float offsetX = -12.55f;
        float offsetY = -3f;

        selectionTex.bind();
        drawQuad(
            selectedX + offsetX,
            selectedY + offsetY,
            size,
            size,
            0f, 1f, 1f, 0f
        );
        glDrawArrays(GL_TRIANGLES, 0, 6);

        for (int i = 0; i < 9; i++) {
            int invSlot = Inventory.HOTBAR_START + i;
            int item = inventory.getItem(invSlot);
            int count = inventory.getCount(invSlot);

            if (item == Chunk.AIR) continue;

            float slotX = startX + i * 40f;
            float slotY = iconY;

            renderHotbarItem(item, count, slotX, slotY, slotSize);
        }

        glBindVertexArray(0);
        glUseProgram(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public void renderHotbarItem(int item, int count, float slotX, float slotY, float slotSize) {
        if (isFlatItem(item)) {
            renderFlatHotbarItem(item, count, slotX, slotY, slotSize);
            return;
        }

        float cx = slotX + slotSize / 1.5f;
        float cy = slotY + slotSize / 4f;

        float nx = (cx / W) * 2f - 1f;
        float ny = 1f - (cy / H) * 2f;
        float ndcSize = (slotSize / W) * 1.65f;

        Texture top = getBlockTopTexture(item);
        Texture side = getBlockSideTexture(item);
        if (top == null || side == null) return;

        float tintR = 1f;
        float tintG = 1f;
        float tintB = 1f;
        boolean tintWholeBlock = false;

        if (item == Chunk.OAK_LEAVES) {
            tintR = 0.45f;
            tintG = 0.85f;
            tintB = 0.45f;
            tintWholeBlock = true;
        } else if (item == Chunk.GRASS) {
            tintR = 0.55f;
            tintG = 0.82f;
            tintB = 0.55f;
        }

        blockRenderer.render(
            nx - ndcSize * 0.74f + 0.012f,
            ny - ndcSize * 0.48f + 0.008f,
            ndcSize,
            top,
            side,
            tintR, tintG, tintB,
            tintWholeBlock
        );

        if (count > 1 && fontRenderer != null) {
            fontRenderer.drawText(
                this,
                String.valueOf(count),
                slotX + HOTBAR_COUNT_OFFSET_X,
                slotY + HOTBAR_COUNT_OFFSET_Y
            );
        }
    }

    public void renderInventoryItem(int item, int count, float slotX, float slotY) {
        if (isFlatItem(item)) {
            renderFlatInventoryItem(item, count, slotX, slotY);
            return;
        }

        float slotSize = 36f;

        float cx = slotX + 32.5f;
        float cy = slotY + 17f;

        float nx = (cx / W) * 2f - 1f;
        float ny = 1f - (cy / H) * 2f;
        float ndcSize = (slotSize / W) * 1.65f;

        Texture top = getBlockTopTexture(item);
        Texture side = getBlockSideTexture(item);
        if (top == null || side == null) return;

        float tintR = 1f;
        float tintG = 1f;
        float tintB = 1f;
        boolean tintWholeBlock = false;

        if (item == Chunk.OAK_LEAVES) {
            tintR = 0.45f;
            tintG = 0.85f;
            tintB = 0.45f;
            tintWholeBlock = true;
        } else if (item == Chunk.GRASS) {
            tintR = 0.55f;
            tintG = 0.82f;
            tintB = 0.55f;
        }

        blockRenderer.render(
            nx - ndcSize * 0.62f + 0.003f,
            ny - ndcSize * 0.38f + 0.001f,
            ndcSize,
            top,
            side,
            tintR, tintG, tintB,
            tintWholeBlock
        );

        if (count > 1 && fontRenderer != null) {
            fontRenderer.drawText(
                this,
                String.valueOf(count),
                slotX + INVENTORY_COUNT_OFFSET_X,
                slotY + INVENTORY_COUNT_OFFSET_Y
            );
        }
    }

    private boolean isFlatItem(int item) {
        return item == ItemTypes.STICK ||
               item == ItemTypes.WOODEN_PICKAXE ||
               item == ItemTypes.WOODEN_SHOVEL ||
               item == ItemTypes.WOODEN_AXE;
    }

    private void renderFlatHotbarItem(int item, int count, float slotX, float slotY, float slotSize) {
        Texture tex = getFlatItemTexture(item);
        if (tex == null) return;

        ItemSpriteBounds bounds = getFlatItemBounds(item);

        glUseProgram(shaderProgram);
        glBindVertexArray(vaoId);
        glUniform1i(glGetUniformLocation(shaderProgram, "tex"), 0);
        glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), 1f);

        tex.bind();

        float itemSize = 29f;

        float scaleX = itemSize / 16f;
        float scaleY = itemSize / 16f;

        float visibleW = bounds.width * scaleX;
        float visibleH = bounds.height * scaleY;

        float offsetX = bounds.minX * scaleX;
        float offsetY = bounds.minY * scaleY;

        float centerX = slotX + slotSize * 0.5f;
        float centerY = slotY + slotSize * 0.5f;

        float drawX = centerX - visibleW * 0.5f - offsetX - 10f;
        float drawY = centerY - visibleH * 0.5f - offsetY;

        drawQuad(drawX, drawY, itemSize, itemSize, 0f, 1f, 1f, 0f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        if (count > 1 && fontRenderer != null) {
            fontRenderer.drawText(
                this,
                String.valueOf(count),
                slotX + HOTBAR_COUNT_OFFSET_X,
                slotY + HOTBAR_COUNT_OFFSET_Y
            );
        }
    }

    private void renderFlatInventoryItem(int item, int count, float slotX, float slotY) {
        Texture tex = getFlatItemTexture(item);
        if (tex == null) return;

        ItemSpriteBounds bounds = getFlatItemBounds(item);

        glUseProgram(shaderProgram);
        glBindVertexArray(vaoId);
        glUniform1i(glGetUniformLocation(shaderProgram, "tex"), 0);
        glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), 1f);

        tex.bind();

        float slotSize = 36f;
        float itemSize = 32f;

        float scaleX = itemSize / 16f;
        float scaleY = itemSize / 16f;

        float visibleW = bounds.width * scaleX;
        float visibleH = bounds.height * scaleY;

        float offsetX = bounds.minX * scaleX;
        float offsetY = bounds.minY * scaleY;

        float centerX = slotX + slotSize * 0.5f;
        float centerY = slotY + slotSize * 0.5f;

        float drawX = centerX - visibleW * 0.5f - offsetX - 3f;
        float drawY = centerY - visibleH * 0.5f - offsetY + 6f;

        drawQuad(drawX, drawY, itemSize, itemSize, 0f, 1f, 1f, 0f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        if (count > 1 && fontRenderer != null) {
            fontRenderer.drawText(
                this,
                String.valueOf(count),
                slotX + INVENTORY_COUNT_OFFSET_X,
                slotY + INVENTORY_COUNT_OFFSET_Y
            );
        }
    }

    private Texture getFlatItemTexture(int item) {
        switch (item) {
            case ItemTypes.STICK:
                return TextureManager.stick;
            case ItemTypes.WOODEN_PICKAXE:
                return TextureManager.woodenPickaxe;
            case ItemTypes.WOODEN_SHOVEL:
                return TextureManager.woodenShovel;
            case ItemTypes.WOODEN_AXE:
                return TextureManager.woodenAxe;
            default:
                return null;
        }
    }

    public ItemSpriteBounds getFlatItemBounds(int item) {
        switch (item) {
            case ItemTypes.STICK:
                return stickBounds;
            case ItemTypes.WOODEN_PICKAXE:
                return woodenPickaxeBounds;
            case ItemTypes.WOODEN_SHOVEL:
                return woodenShovelBounds;
            case ItemTypes.WOODEN_AXE:
                return woodenAxeBounds;
            default:
                return stickBounds;
        }
    }

    private Texture getBlockTopTexture(int item) {
        switch (item) {
            case Chunk.GRASS: return TextureManager.grassTop;
            case Chunk.DIRT: return TextureManager.dirt;
            case Chunk.STONE: return TextureManager.stone;
            case Chunk.OAK_LOG: return TextureManager.oakLogTop;
            case Chunk.OAK_PLANKS: return TextureManager.oakPlanks;
            case Chunk.OAK_LEAVES: return TextureManager.oakLeaves;
            case Chunk.CRAFTING_TABLE: return TextureManager.craftingTableTop;
            case ItemTypes.COBBLESTONE: return TextureManager.cobblestone;
            default: return null;
        }
    }

    private Texture getBlockSideTexture(int item) {
        switch (item) {
            case Chunk.GRASS: return TextureManager.grassSide;
            case Chunk.DIRT: return TextureManager.dirt;
            case Chunk.STONE: return TextureManager.stone;
            case Chunk.OAK_LOG: return TextureManager.oakLogSide;
            case Chunk.OAK_PLANKS: return TextureManager.oakPlanks;
            case Chunk.OAK_LEAVES: return TextureManager.oakLeaves;
            case Chunk.CRAFTING_TABLE: return TextureManager.craftingTableSide;
            case ItemTypes.COBBLESTONE: return TextureManager.cobblestone;
            default: return null;
        }
    }

    public void drawTextQuad(float x, float y, float w, float h) {
        glUseProgram(shaderProgram);
        glBindVertexArray(vaoId);
        glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), 1f);
        glUniform1i(glGetUniformLocation(shaderProgram, "tex"), 0);

        drawQuad(x, y, w, h, 0f, 0f, 1f, 1f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glBindVertexArray(0);
    }

    public void drawTextQuadFlipped(float x, float y, float w, float h) {
        glUseProgram(shaderProgram);
        glBindVertexArray(vaoId);
        glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), 1f);
        glUniform1i(glGetUniformLocation(shaderProgram, "tex"), 0);

        drawQuad(x, y, w, h, 0f, 1f, 1f, 0f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glBindVertexArray(0);
    }

    @Override
    public void drawColoredQuad(float x, float y, float w, float h, float r, float g, float b, float a) {
        glUseProgram(shaderProgram);
        glBindVertexArray(vaoId);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        glUniform1i(glGetUniformLocation(shaderProgram, "tex"), 0);
        glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), a);

        hotbarTex.bind();
        drawQuad(x, y, w, h, 0f, 0f, 0f, 0f);

        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }
}