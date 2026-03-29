package controls;
import static org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushAttrib;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL20.glUseProgram;

import entity.Player;
import render.Texture;
import inventory.Hotbar;
import items.HeldItemRenderer;

public class SteveRenderer {

    private Texture skin;
    private final HeldItemRenderer heldItemRenderer = new HeldItemRenderer();
    private float breakSwingProgress = 0f;
    private boolean breaking = false;

    private float placeProgress = 0f;
    private boolean placing = false;

    private float lastYaw = 0f;
    private float lastPitch = 0f;

    private float swayYaw = 0f;
    private float swayPitch = 0f;

    private boolean firstFrame = true;

    private float punchProgress = 0f;
    private boolean punching = false;
    
    

    public void init() {
        skin = new Texture("textures/environment/steve.png");
    }

    public void render(float x, float y, float z, float time) {
        glPushMatrix();
        try {
            glTranslatef(x, -0.37f, z);
            glRotatef(time * 40f, 0f, 1f, 0f);
            glScalef(1.24f, 1.24f, 1.24f);

            glEnable(GL_TEXTURE_2D);
            glDisable(GL_CULL_FACE);
            glDisable(GL_BLEND);
            glColor4f(1f, 1f, 1f, 1f);

            skin.bind();

            // Body
            drawCuboid(0f, 18f, 0f, 8f, 12f, 4f,
                new FaceUV(20, 20, 8, 12),
                new FaceUV(32, 20, 8, 12),
                new FaceUV(28, 20, 4, 12),
                new FaceUV(16, 20, 4, 12),
                new FaceUV(20, 16, 8, 4),
                new FaceUV(28, 16, 8, 4)
            );

            // Head
            drawCuboid(0f, 28f, 0f, 8f, 8f, 8f,
                new FaceUV(8, 8, 8, 8),
                new FaceUV(24, 8, 8, 8),
                new FaceUV(0, 8, 8, 8),
                new FaceUV(16, 8, 8, 8),
                new FaceUV(8, 0, 8, 8),
                new FaceUV(16, 0, 8, 8)
            );

            // Right arm
            drawCuboid(-6f, 18f, 0f, 4f, 12f, 4f,
                new FaceUV(44, 20, 4, 12),
                new FaceUV(52, 20, 4, 12),
                new FaceUV(48, 20, 4, 12),
                new FaceUV(40, 20, 4, 12),
                new FaceUV(44, 16, 4, 4),
                new FaceUV(48, 16, 4, 4)
            );

            // Left arm
            drawCuboid(6f, 18f, 0f, 4f, 12f, 4f,
                new FaceUV(36, 52, 4, 12),
                new FaceUV(44, 52, 4, 12),
                new FaceUV(40, 52, 4, 12),
                new FaceUV(32, 52, 4, 12),
                new FaceUV(36, 48, 4, 4),
                new FaceUV(40, 48, 4, 4)
            );

            // Right leg
            drawCuboid(-2f, 6f, 0f, 4f, 12f, 4f,
                new FaceUV(4, 20, 4, 12),
                new FaceUV(12, 20, 4, 12),
                new FaceUV(8, 20, 4, 12),
                new FaceUV(0, 20, 4, 12),
                new FaceUV(4, 16, 4, 4),
                new FaceUV(8, 16, 4, 4)
            );

            // Left leg
            drawCuboid(2f, 6f, 0f, 4f, 12f, 4f,
                new FaceUV(20, 52, 4, 12),
                new FaceUV(28, 52, 4, 12),
                new FaceUV(24, 52, 4, 12),
                new FaceUV(16, 52, 4, 12),
                new FaceUV(20, 48, 4, 4),
                new FaceUV(24, 48, 4, 4)
            );

        } finally {
            glColor4f(1f, 1f, 1f, 1f);
            glPopMatrix();
        }
    }

    public void renderFirstPersonArm(Camera camera, Player player, Hotbar hotbar) {
        glPushAttrib(GL_ALL_ATTRIB_BITS);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();

        float aspect = 1024f / 768f;
        float near = 0.05f;
        float far = 10f;
        float fovY = 70f;
        float top = (float) Math.tan(Math.toRadians(fovY * 0.5f)) * near;
        float bottom = -top;
        float right = top * aspect;
        float left = -right;

        glFrustum(left, right, bottom, top, near, far);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        try {
            glUseProgram(0);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_CULL_FACE);
            glDisable(GL_BLEND);
            glColor4f(1f, 1f, 1f, 1f);

            skin.bind();

            float walk = player.getWalkDistance();
            float speed = player.getHorizontalSpeed();

            float bobAmount = Math.min(speed / 4.6f, 1f);
            bobAmount *= bobAmount;

            float armBobX = 0f;
            float armBobY = (float) Math.sin(walk * 2f) * 0.033f * bobAmount;
            float walkRoll = 0f;

            float swing = breakSwingProgress;
            float swingSin = (float) Math.sin(swing * (float) Math.PI);
            float swingSin2 = (float) Math.sin(Math.sqrt(Math.max(swing, 0f)) * (float) Math.PI);

            float place = placing ? placeProgress : 0f;
            float placeSin = (float) Math.sin(place * (float) Math.PI);
            float placeSin2 = (float) Math.sin(Math.sqrt(Math.max(place, 0f)) * (float) Math.PI);

            float punch = punching ? punchProgress : 0f;
            float punchSin = (float) Math.sin(punch * (float) Math.PI);
            float punchSin2 = (float) Math.sin(Math.sqrt(Math.max(punch, 0f)) * (float) Math.PI);

            float yaw = camera.yaw;
            float pitch = camera.pitch;

            if (firstFrame) {
                lastYaw = yaw;
                lastPitch = pitch;
                firstFrame = false;
            }

            float yawDelta = wrapDegrees(yaw - lastYaw);
            float pitchDelta = pitch - lastPitch;

            lastYaw = yaw;
            lastPitch = pitch;

            yawDelta = Math.max(-15f, Math.min(15f, yawDelta));
            pitchDelta = Math.max(-15f, Math.min(15f, pitchDelta));

            swayYaw += (yawDelta - swayYaw) * 0.14f;
            swayPitch += (pitchDelta - swayPitch) * 0.14f;

            swayYaw *= 0.94f;
            swayPitch *= 0.94f;

            float lagX = -swayYaw * 0.013f;
            float lagY = -swayPitch * 0.009f;
            float lagZ = Math.abs(swayYaw) * 0.002f;

            // shared hand/item transform
            glTranslatef(1.15f + armBobX, -0.65f + armBobY, -0.9f);
            glRotatef(walkRoll, 0f, 0f, 1f);

            glTranslatef(lagX, lagY, lagZ);
            glRotatef(-swayYaw * 0.18f, 0f, 0f, 1f);
            glRotatef(swayPitch * 0.15f, 1f, 0f, 0f);

            if (breaking) {
                glTranslatef(-swingSin * 0.035f, -swingSin2 * 0.12f, 0.02f - swingSin * 0.02f);
                glRotatef(-swingSin * 6f, 0f, 0f, 1f);
                glRotatef(-swingSin2 * 34f, 1f, 0f, 0f);
                glRotatef(swingSin * 4f, 0f, 1f, 0f);
            } else if (placing) {
                glTranslatef(-placeSin * 0.026f, -placeSin2 * 0.085f, 0.012f - placeSin * 0.014f);
                glRotatef(-placeSin * 4.5f, 0f, 0f, 1f);
                glRotatef(-placeSin2 * 22f, 1f, 0f, 0f);
                glRotatef(placeSin * 3f, 0f, 1f, 0f);
            } else if (punching) {
                glTranslatef(-punchSin * 0.030f, -punchSin2 * 0.10f, 0.015f - punchSin * 0.015f);
                glRotatef(-punchSin * 5f, 0f, 0f, 1f);
                glRotatef(-punchSin2 * 26f, 1f, 0f, 0f);
                glRotatef(punchSin * 3f, 0f, 1f, 0f);
            }

            glRotatef(-camera.roll * 1.2f, 0f, 0f, 1f);

            // ARM
            glPushMatrix();
            glRotatef(-18f, 1f, 1f, 0f);
            glRotatef(-5f, 1f, 0f, 0f);
            glRotatef(-165f, 0f, 0f, 1f);
            glScalef(0.08f, 0.08f, 0.08f);

            drawCuboid(5f, 0f, 0f, 4f, 12f, 4f,
                new FaceUV(44, 20, 4, 12),
                new FaceUV(52, 20, 4, 12),
                new FaceUV(48, 20, 4, 12),
                new FaceUV(40, 20, 4, 12),
                new FaceUV(44, 16, 4, 4),
                new FaceUV(48, 16, 4, 4)
            );
            glPopMatrix();

            // HELD ITEM
            int heldItem = hotbar.getSelectedItem();
            if (heldItem != 0) {
            	glPushMatrix();

            	// FORCE IT INTO YOUR FACE
            	glTranslatef(-0.4f, 0.4f, -0.4f);   // super close to camera
            	glScalef(1.5f, 1.5f, 1.5f);          // huge

            	glRotatef(0f, 0f, 1f, 0f);

            	heldItemRenderer.render(heldItem);

            	glPopMatrix();
            }

        } finally {
            glMatrixMode(GL_MODELVIEW);
            glPopMatrix();

            glMatrixMode(GL_PROJECTION);
            glPopMatrix();

            glMatrixMode(GL_MODELVIEW);
            glColor4f(1f, 1f, 1f, 1f);
            glPopAttrib();
        }
    }

    public void setBreaking(boolean breaking) {
        this.breaking = breaking;
        if (breaking) {
            punching = false;
            punchProgress = 0f;
        }
    }

    public void updateSwing(float deltaTime) {
        if (breaking) {
            breakSwingProgress += deltaTime * 4.8f;
            if (breakSwingProgress >= 1f) {
                breakSwingProgress = 0f;
            }
        } else {
            breakSwingProgress = 0f;
        }

        if (placing) {
            placeProgress += deltaTime * 5.2f;
            if (placeProgress >= 1f) {
                placeProgress = 1f;
                placing = false;
            }
        } else {
            placeProgress = 0f;
        }

        if (punching) {
            punchProgress += deltaTime * 5.8f;
            if (punchProgress >= 1f) {
                punchProgress = 0f;
                punching = false;
            }
        } else {
            punchProgress = 0f;
        }
    }

    public void startPlace() {
        if (!placing) {
            placing = true;
            placeProgress = 0f;
        }
    }

    public void startPunch() {
        if (placing || punching) return;

        breaking = false;
        breakSwingProgress = 0f;

        punching = true;
        punchProgress = 0f;
    }

    private void drawCuboid(
        float cx, float cy, float cz,
        float w, float h, float d,
        FaceUV front, FaceUV back, FaceUV left, FaceUV right, FaceUV top, FaceUV bottom
    ) {
        float x0 = cx - w / 2f;
        float x1 = cx + w / 2f;
        float y0 = cy - h / 2f;
        float y1 = cy + h / 2f;
        float z0 = cz - d / 2f;
        float z1 = cz + d / 2f;

        glBegin(GL_QUADS);

        shade(1.0f);
        uv(front, 0); glVertex3f(x0, y0, z1);
        uv(front, 1); glVertex3f(x1, y0, z1);
        uv(front, 2); glVertex3f(x1, y1, z1);
        uv(front, 3); glVertex3f(x0, y1, z1);

        shade(0.72f);
        uv(back, 0); glVertex3f(x1, y0, z0);
        uv(back, 1); glVertex3f(x0, y0, z0);
        uv(back, 2); glVertex3f(x0, y1, z0);
        uv(back, 3); glVertex3f(x1, y1, z0);

        shade(0.82f);
        uv(left, 0); glVertex3f(x0, y0, z0);
        uv(left, 1); glVertex3f(x0, y0, z1);
        uv(left, 2); glVertex3f(x0, y1, z1);
        uv(left, 3); glVertex3f(x0, y1, z0);

        shade(0.62f);
        uv(right, 0); glVertex3f(x1, y0, z1);
        uv(right, 1); glVertex3f(x1, y0, z0);
        uv(right, 2); glVertex3f(x1, y1, z0);
        uv(right, 3); glVertex3f(x1, y1, z1);

        shade(1.0f);
        uv(top, 0); glVertex3f(x0, y1, z0);
        uv(top, 1); glVertex3f(x1, y1, z0);
        uv(top, 2); glVertex3f(x1, y1, z1);
        uv(top, 3); glVertex3f(x0, y1, z1);

        shade(0.5f);
        uv(bottom, 0); glVertex3f(x0, y0, z1);
        uv(bottom, 1); glVertex3f(x1, y0, z1);
        uv(bottom, 2); glVertex3f(x1, y0, z0);
        uv(bottom, 3); glVertex3f(x0, y0, z0);

        glEnd();
    }

    private void shade(float s) {
        glColor4f(s, s, s, 1f);
    }

    private void uv(FaceUV face, int corner) {
        float texSize = 64f;

        float u0 = face.x / texSize;
        float v0 = 1f - (face.y / texSize);
        float u1 = (face.x + face.w) / texSize;
        float v1 = 1f - ((face.y + face.h) / texSize);

        switch (corner) {
            case 0: glTexCoord2f(u0, v1); break;
            case 1: glTexCoord2f(u1, v1); break;
            case 2: glTexCoord2f(u1, v0); break;
            case 3: glTexCoord2f(u0, v0); break;
        }
    }

    private static class FaceUV {
        final float x, y, w, h;

        FaceUV(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private float wrapDegrees(float angle) {
        while (angle <= -180f) angle += 360f;
        while (angle > 180f) angle -= 360f;
        return angle;
    }
}