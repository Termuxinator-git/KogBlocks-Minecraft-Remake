package items;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;

import entity.ItemEntity;
import render.Texture;
import render.TextureManager;
import world.Chunk;

public class DroppedItemRenderer {

    private final ExtrudedItemRenderer stickRenderer =
            new ExtrudedItemRenderer("textures/items/nature/stick.png");
    private final ExtrudedItemRenderer woodenPickaxeRenderer =
            new ExtrudedItemRenderer("textures/items/tools/wooden_pickaxe.png");
    private final ExtrudedItemRenderer woodenShovelRenderer =
            new ExtrudedItemRenderer("textures/items/tools/wooden_shovel.png");
    private final ExtrudedItemRenderer woodenAxeRenderer =
            new ExtrudedItemRenderer("textures/items/tools/wooden_axe.png");

    public DroppedItemRenderer() {
    }

    public void render(ItemEntity item, float partialTicks) {
        if (item == null) return;
        if (item.count <= 0) return;
        if (item.itemId == Chunk.AIR) return;

        ItemDef def = ItemRegistry.get(item.itemId);
        if (def == null) {
            System.out.println("Missing item def for itemId: " + item.itemId);
            return;
        }

        glPushMatrix();
        try {
            float time = (float) glfwGetTime() + partialTicks;

            float bob = (float) Math.sin(time * 2.0f) * 0.08f + 0.1f;
            float spin = (time * 80.0f) % 360.0f;

            glTranslatef(item.position.x, item.position.y + bob, item.position.z);
            glRotatef(spin, 0f, 1f, 0f);

            float scale = 0.25f;
            glScalef(scale, scale, scale);

            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glColor4f(1f, 1f, 1f, 1f);

            if (def.renderKind == RenderKind.BLOCK) {
                glEnable(GL_CULL_FACE);
                glCullFace(GL_BACK);
                glDisable(GL_BLEND);
                renderMiniBlock(item.itemId);
            } else {
                glDisable(GL_BLEND);
                glDisable(GL_CULL_FACE);

                if (item.itemId == ItemTypes.STICK ||
                    item.itemId == ItemTypes.WOODEN_PICKAXE ||
                    item.itemId == ItemTypes.WOODEN_SHOVEL ||
                    item.itemId == ItemTypes.WOODEN_AXE) {

                    glScalef(1.4f, 1.4f, 1.4f);

                    switch (item.itemId) {
                        case ItemTypes.STICK:
                            stickRenderer.render();
                            break;
                        case ItemTypes.WOODEN_PICKAXE:
                            woodenPickaxeRenderer.render();
                            break;
                        case ItemTypes.WOODEN_SHOVEL:
                            woodenShovelRenderer.render();
                            break;
                        case ItemTypes.WOODEN_AXE:
                            woodenAxeRenderer.render();
                            break;
                    }
                } else {
                    renderFlatItem(def.texture);
                }
            }
        } finally {
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
            glDisable(GL_BLEND);
            glCullFace(GL_BACK);
            glColor4f(1f, 1f, 1f, 1f);
            glPopMatrix();
        }
    }

    private void renderMiniBlock(int blockId) {
        Texture top = getTopTexture(blockId);
        Texture bottom = getBottomTexture(blockId);
        Texture side = getSideTexture(blockId);

        float min = -0.5f;
        float max = 0.5f;

        if (top != null) top.bind();
        applyFaceColor(blockId, Face.TOP);
        glBegin(GL_QUADS);
        glNormal3f(0f, 1f, 0f);
        glTexCoord2f(0f, 1f); glVertex3f(min, max, min);
        glTexCoord2f(1f, 1f); glVertex3f(min, max, max);
        glTexCoord2f(1f, 0f); glVertex3f(max, max, max);
        glTexCoord2f(0f, 0f); glVertex3f(max, max, min);
        glEnd();

        if (bottom != null) bottom.bind();
        applyFaceColor(blockId, Face.BOTTOM);
        glBegin(GL_QUADS);
        glNormal3f(0f, -1f, 0f);
        glTexCoord2f(0f, 1f); glVertex3f(min, min, max);
        glTexCoord2f(1f, 1f); glVertex3f(min, min, min);
        glTexCoord2f(1f, 0f); glVertex3f(max, min, min);
        glTexCoord2f(0f, 0f); glVertex3f(max, min, max);
        glEnd();

        if (side != null) side.bind();
        applyFaceColor(blockId, Face.SIDE);
        glBegin(GL_QUADS);
        glNormal3f(0f, 0f, 1f);
        glTexCoord2f(0f, 0f); glVertex3f(min, min, max);
        glTexCoord2f(1f, 0f); glVertex3f(max, min, max);
        glTexCoord2f(1f, 1f); glVertex3f(max, max, max);
        glTexCoord2f(0f, 1f); glVertex3f(min, max, max);
        glEnd();

        if (side != null) side.bind();
        applyFaceColor(blockId, Face.SIDE);
        glBegin(GL_QUADS);
        glNormal3f(0f, 0f, -1f);
        glTexCoord2f(0f, 0f); glVertex3f(max, min, min);
        glTexCoord2f(1f, 0f); glVertex3f(min, min, min);
        glTexCoord2f(1f, 1f); glVertex3f(min, max, min);
        glTexCoord2f(0f, 1f); glVertex3f(max, max, min);
        glEnd();

        if (side != null) side.bind();
        applyFaceColor(blockId, Face.SIDE);
        glBegin(GL_QUADS);
        glNormal3f(-1f, 0f, 0f);
        glTexCoord2f(0f, 0f); glVertex3f(min, min, min);
        glTexCoord2f(1f, 0f); glVertex3f(min, min, max);
        glTexCoord2f(1f, 1f); glVertex3f(min, max, max);
        glTexCoord2f(0f, 1f); glVertex3f(min, max, min);
        glEnd();

        if (side != null) side.bind();
        applyFaceColor(blockId, Face.SIDE);
        glBegin(GL_QUADS);
        glNormal3f(1f, 0f, 0f);
        glTexCoord2f(0f, 0f); glVertex3f(max, min, max);
        glTexCoord2f(1f, 0f); glVertex3f(max, min, min);
        glTexCoord2f(1f, 1f); glVertex3f(max, max, min);
        glTexCoord2f(0f, 1f); glVertex3f(max, max, max);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);
    }

    private Texture getTopTexture(int blockId) {
        if (blockId == Chunk.GRASS) return TextureManager.grassTop;
        if (blockId == Chunk.DIRT) return TextureManager.dirt;
        if (blockId == Chunk.STONE) return TextureManager.stone;
        if (blockId == Chunk.COBBLESTONE) return TextureManager.cobblestone;
        if (blockId == Chunk.OAK_LOG) return TextureManager.oakLogTop;
        if (blockId == Chunk.OAK_LEAVES) return TextureManager.oakLeaves;
        if (blockId == Chunk.OAK_PLANKS) return TextureManager.oakPlanks;
        if (blockId == Chunk.CRAFTING_TABLE) return TextureManager.craftingTableTop;
        return null;
    }

    private Texture getBottomTexture(int blockId) {
        if (blockId == Chunk.GRASS) return TextureManager.dirt;
        if (blockId == Chunk.DIRT) return TextureManager.dirt;
        if (blockId == Chunk.STONE) return TextureManager.stone;
        if (blockId == Chunk.COBBLESTONE) return TextureManager.cobblestone;
        if (blockId == Chunk.OAK_LOG) return TextureManager.oakLogTop;
        if (blockId == Chunk.OAK_LEAVES) return TextureManager.oakLeaves;
        if (blockId == Chunk.OAK_PLANKS) return TextureManager.oakPlanks;
        if (blockId == Chunk.CRAFTING_TABLE) return TextureManager.oakPlanks;
        return null;
    }

    private Texture getSideTexture(int blockId) {
        if (blockId == Chunk.GRASS) return TextureManager.grassSide;
        if (blockId == Chunk.DIRT) return TextureManager.dirt;
        if (blockId == Chunk.STONE) return TextureManager.stone;
        if (blockId == Chunk.COBBLESTONE) return TextureManager.cobblestone;
        if (blockId == Chunk.OAK_LOG) return TextureManager.oakLogSide;
        if (blockId == Chunk.OAK_LEAVES) return TextureManager.oakLeaves;
        if (blockId == Chunk.OAK_PLANKS) return TextureManager.oakPlanks;
        if (blockId == Chunk.CRAFTING_TABLE) return TextureManager.craftingTableSide;
        return null;
    }

    private enum Face {
        TOP,
        BOTTOM,
        SIDE
    }

    private void applyFaceColor(int blockId, Face face) {
        glColor4f(1f, 1f, 1f, 1f);

        if (blockId == Chunk.GRASS && face == Face.TOP) {
            glColor4f(0.55f, 0.80f, 0.35f, 1f);
            return;
        }

        if (blockId == Chunk.OAK_LEAVES) {
            glColor4f(0.55f, 0.80f, 0.35f, 1f);
        }
    }

    private void renderFlatItem(Texture tex) {
        if (tex == null) return;

        tex.bind();

        float s = 0.5f;
        float depth = 0.08f;

        glEnable(GL_CULL_FACE);

        glBegin(GL_QUADS);
        glNormal3f(0, 0, 1);
        glTexCoord2f(0, 0); glVertex3f(-s, -s, depth);
        glTexCoord2f(1, 0); glVertex3f( s, -s, depth);
        glTexCoord2f(1, 1); glVertex3f( s,  s, depth);
        glTexCoord2f(0, 1); glVertex3f(-s,  s, depth);
        glEnd();

        glBegin(GL_QUADS);
        glNormal3f(0, 0, -1);
        glTexCoord2f(1, 0); glVertex3f(-s, -s, -depth);
        glTexCoord2f(1, 1); glVertex3f(-s,  s, -depth);
        glTexCoord2f(0, 1); glVertex3f( s,  s, -depth);
        glTexCoord2f(0, 0); glVertex3f( s, -s, -depth);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glColor4f(0.2f, 0.2f, 0.2f, 1f);

        glBegin(GL_QUADS);
        glVertex3f(-s, -s, -depth);
        glVertex3f(-s, -s,  depth);
        glVertex3f(-s,  s,  depth);
        glVertex3f(-s,  s, -depth);
        glEnd();

        glBegin(GL_QUADS);
        glVertex3f(s, -s,  depth);
        glVertex3f(s, -s, -depth);
        glVertex3f(s,  s, -depth);
        glVertex3f(s,  s,  depth);
        glEnd();

        glBegin(GL_QUADS);
        glVertex3f(-s, s,  depth);
        glVertex3f(s,  s,  depth);
        glVertex3f(s,  s, -depth);
        glVertex3f(-s, s, -depth);
        glEnd();

        glBegin(GL_QUADS);
        glVertex3f(-s, -s, -depth);
        glVertex3f(s,  -s, -depth);
        glVertex3f(s,  -s,  depth);
        glVertex3f(-s, -s,  depth);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glColor4f(1f, 1f, 1f, 1f);
    }
}