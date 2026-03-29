package items;

import static org.lwjgl.opengl.GL11.*;

import render.TextureManager;

public class HeldItemRenderer {

    private final ExtrudedItemRenderer stickRenderer;
    private final ExtrudedItemRenderer woodenPickaxeRenderer;
    private final ExtrudedItemRenderer woodenShovelRenderer;
    private final ExtrudedItemRenderer woodenAxeRenderer;

    public HeldItemRenderer() {
        stickRenderer = new ExtrudedItemRenderer("textures/items/nature/stick.png");
        woodenPickaxeRenderer = new ExtrudedItemRenderer("textures/items/tools/wooden_pickaxe.png");
        woodenShovelRenderer  = new ExtrudedItemRenderer("textures/items/tools/wooden_shovel.png");
        woodenAxeRenderer     = new ExtrudedItemRenderer("textures/items/tools/wooden_axe.png");
    }

    public void render(int itemId) {
        if (itemId == 0) return;

        ItemDef def = ItemRegistry.get(itemId);
        if (def == null) return;

        glPushMatrix();
        try {
            if (def.renderKind == RenderKind.BLOCK) {
                renderBlock(itemId);
            } else {
                renderFlat(itemId);
            }
        } finally {
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_CULL_FACE);
            glDisable(GL_BLEND);
            glColor4f(1f, 1f, 1f, 1f);
            glPopMatrix();
        }
    }

    private void renderFlat(int itemId) {
        glScalef(0.42f, 0.42f, 0.42f);
        glRotatef(110f, 0f, 1f, 0f);
        glRotatef(20f, 1f, 0f, 0f);
        glRotatef(10f, 0f, 0f, 1f);

        switch (itemId) {
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
    }

    private void renderBlock(int itemId) {
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);

        glScalef(0.22f, 0.22f, 0.22f);
        glRotatef(25f, 1f, 0f, 0f);
        glRotatef(45f, 0f, 1f, 0f);

        switch (itemId) {
            case ItemTypes.DIRT:
                drawCube(TextureManager.dirt, TextureManager.dirt, TextureManager.dirt,
                        1f, 1f, 1f, false);
                break;

            case ItemTypes.GRASS:
                drawGrassCube();
                break;

            case ItemTypes.STONE:
                drawCube(TextureManager.stone, TextureManager.stone, TextureManager.stone,
                        1f, 1f, 1f, false);
                break;

            case ItemTypes.COBBLESTONE:
                drawCube(TextureManager.cobblestone, TextureManager.cobblestone, TextureManager.cobblestone,
                        1f, 1f, 1f, false);
                break;

            case ItemTypes.OAK_LOG:
                drawCube(TextureManager.oakLogTop, TextureManager.oakLogTop, TextureManager.oakLogSide,
                        1f, 1f, 1f, false);
                break;

            case ItemTypes.OAK_LEAVES:
                drawCube(TextureManager.oakLeaves, TextureManager.oakLeaves, TextureManager.oakLeaves,
                        0.55f, 0.80f, 0.35f, true);
                break;

            case ItemTypes.OAK_PLANKS:
                drawCube(TextureManager.oakPlanks, TextureManager.oakPlanks, TextureManager.oakPlanks,
                        1f, 1f, 1f, false);
                break;

            case ItemTypes.CRAFTING_TABLE:
                drawCraftingTable();
                break;
        }
    }

    private void drawCraftingTable() {
        glEnable(GL_TEXTURE_2D);

        TextureManager.craftingTableTop.bind();
        shade(1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glEnd();

        TextureManager.oakPlanks.bind();
        shade(0.5f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f, -0.5f, -0.5f);
        glEnd();

        TextureManager.craftingTableFront.bind();
        shade(1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glEnd();

        TextureManager.craftingTableSide.bind();
        shade(0.72f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f( 0.5f,  0.5f, -0.5f);
        glEnd();

        TextureManager.craftingTableSide.bind();
        shade(0.82f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f, -0.5f);
        glEnd();

        TextureManager.craftingTableSide.bind();
        shade(0.62f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glEnd();
    }

    private void drawGrassCube() {
        glEnable(GL_TEXTURE_2D);

        TextureManager.grassTop.bind();
        shade(0.55f, 0.80f, 0.35f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glEnd();

        TextureManager.dirt.bind();
        shade(0.5f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f, -0.5f, -0.5f);
        glEnd();

        TextureManager.grassSide.bind();
        shade(1f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glEnd();

        shade(0.72f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f( 0.5f,  0.5f, -0.5f);
        glEnd();

        shade(0.82f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f, -0.5f);
        glEnd();

        shade(0.62f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glEnd();
    }

    private void drawCube(render.Texture top, render.Texture bottom, render.Texture side,
                          float r, float g, float b, boolean tintAll) {
        glEnable(GL_TEXTURE_2D);

        top.bind();
        if (tintAll) shade(1.0f * r, 1.0f * g, 1.0f * b);
        else         shade(1.0f, 1.0f, 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glEnd();

        bottom.bind();
        if (tintAll) shade(0.5f * r, 0.5f * g, 0.5f * b);
        else         shade(0.5f, 0.5f, 0.5f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f, -0.5f, -0.5f);
        glEnd();

        side.bind();
        if (tintAll) shade(1.0f * r, 1.0f * g, 1.0f * b);
        else         shade(1.0f, 1.0f, 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glEnd();

        if (tintAll) shade(0.72f * r, 0.72f * g, 0.72f * b);
        else         shade(0.72f, 0.72f, 0.72f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f(-0.5f,  0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f( 0.5f,  0.5f, -0.5f);
        glEnd();

        if (tintAll) shade(0.82f * r, 0.82f * g, 0.82f * b);
        else         shade(0.82f, 0.82f, 0.82f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 0); glVertex3f(-0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 1); glVertex3f(-0.5f,  0.5f,  0.5f);
        glTexCoord2f(0, 1); glVertex3f(-0.5f,  0.5f, -0.5f);
        glEnd();

        if (tintAll) shade(0.62f * r, 0.62f * g, 0.62f * b);
        else         shade(0.62f, 0.62f, 0.62f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( 0.5f, -0.5f,  0.5f);
        glTexCoord2f(1, 0); glVertex3f( 0.5f, -0.5f, -0.5f);
        glTexCoord2f(1, 1); glVertex3f( 0.5f,  0.5f, -0.5f);
        glTexCoord2f(0, 1); glVertex3f( 0.5f,  0.5f,  0.5f);
        glEnd();
    }

    private void shade(float v) {
        glColor4f(v, v, v, 1f);
    }

    private void shade(float r, float g, float b) {
        glColor4f(r, g, b, 1f);
    }
}