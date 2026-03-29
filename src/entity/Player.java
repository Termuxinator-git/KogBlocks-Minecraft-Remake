package entity;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.joml.Vector3f;

import controls.Camera;
import core.Input;
import world.Chunk;
import world.World;

public class Player {

    public Vector3f position;
    private Vector3f velocity;

    public static final float WIDTH = 0.6f;
    public static final float HEIGHT = 1.8f;

    public boolean isSprinting = false;
    public boolean isCrouching = false;

    private static final float GRAVITY = -39f;
    private static final float JUMP_FORCE = 10.4f;
    private static final float WALK_SPEED = 4.6f;
    private static final float SPRINT_SPEED = 6.6f;
    private static final float CROUCH_SPEED = WALK_SPEED * 0.3f;
    private static final float FALL_DRAG = 0.995f;
    private static final float MAX_FALL_SPEED = -38f;
    private static final float DOUBLE_TAP_TIME = 0.25f;

    private boolean onGround = false;
    private float walkDistance = 0f;

    private float lastWPressTime = -999f;
    private float timeSinceStart = 0f;
    private boolean doubleTapSprintActive = false;

    private float currentEyeHeight = HEIGHT - 0.1f;

    public Player(World world) {
        int spawnX = 8;
        int spawnZ = 8;

        int surfaceY = Chunk.getHeight(spawnX, spawnZ);

        position = new Vector3f(spawnX + 0.5f, surfaceY + 2f, spawnZ + 0.5f);
        velocity = new Vector3f(0, 0, 0);
    }

    public int findSpawnY(World world, int x, int z) {
        for (int y = 255; y >= 0; y--) {
            if (world.getBlock(x, y, z) == Chunk.GRASS &&
                world.getBlock(x, y + 1, z) == Chunk.AIR &&
                world.getBlock(x, y + 2, z) == Chunk.AIR) {
                return y + 1;
            }
        }

        return 100;
    }

    public void update(float deltaTime, Camera camera, World world) {
        timeSinceStart += deltaTime;

        camera.update();

        float yawRad = (float) Math.toRadians(camera.yaw);

        Vector3f forward = new Vector3f(
            (float) Math.cos(yawRad), 0, (float) Math.sin(yawRad)
        ).normalize();

        Vector3f right = new Vector3f(-forward.z, 0, forward.x);

        Vector3f moveDir = new Vector3f(0, 0, 0);

        if (Input.isKeyDown(GLFW_KEY_W)) moveDir.add(forward);
        if (Input.isKeyDown(GLFW_KEY_S)) moveDir.sub(forward);
        if (Input.isKeyDown(GLFW_KEY_D)) moveDir.add(right);
        if (Input.isKeyDown(GLFW_KEY_A)) moveDir.sub(right);

        boolean wantsToMove = moveDir.length() > 0f;
        boolean movingForward = Input.isKeyDown(GLFW_KEY_W);
        boolean ctrlSprint = Input.isKeyDown(GLFW_KEY_LEFT_CONTROL);

        if (Input.isKeyJustPressed(GLFW_KEY_W)) {
            if (timeSinceStart - lastWPressTime <= DOUBLE_TAP_TIME) {
                doubleTapSprintActive = true;
            }
            lastWPressTime = timeSinceStart;
        }

        isCrouching = Input.isKeyDown(GLFW_KEY_LEFT_SHIFT);

        if (!movingForward || isCrouching) {
            doubleTapSprintActive = false;
        }

        boolean doubleTapSprint = doubleTapSprintActive && movingForward;
        isSprinting = !isCrouching && movingForward && wantsToMove && (ctrlSprint || doubleTapSprint);

        float speed = WALK_SPEED;
        if (isCrouching) {
            speed = CROUCH_SPEED;
        } else if (isSprinting) {
            speed = SPRINT_SPEED;
        }

        if (wantsToMove) {
            moveDir.normalize().mul(speed);
        }

        float accel = isSprinting ? 16f : 12f;
        if (isCrouching) {
            accel = 10f;
        }

        velocity.x += (moveDir.x - velocity.x) * accel * deltaTime;
        velocity.z += (moveDir.z - velocity.z) * accel * deltaTime;

        float friction = onGround ? 5f : 0.6f;

        velocity.x -= velocity.x * friction * deltaTime;
        velocity.z -= velocity.z * friction * deltaTime;

        if (!onGround) {
            velocity.y += GRAVITY * deltaTime;
            velocity.y *= FALL_DRAG;

            if (velocity.y < MAX_FALL_SPEED) {
                velocity.y = MAX_FALL_SPEED;
            }
        }

        if (Input.isKeyDown(GLFW_KEY_SPACE) && onGround && !isCrouching) {
            velocity.y = JUMP_FORCE;
            onGround = false;

            if (isSprinting) {
                velocity.x += Math.cos(yawRad) * 1.2f;
                velocity.z += Math.sin(yawRad) * 1.2f;
            }
        }

        moveWithCollision(deltaTime, world);

        float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (horizontalSpeed > 0.01f && onGround) {
            walkDistance += horizontalSpeed * deltaTime * 3.5f;
        }

        float bobX = (float) Math.sin(walkDistance) * 0.010f;
        float bobY = Math.abs((float) Math.cos(walkDistance)) * (isCrouching ? 0.03f : 0.14f);

        float targetEyeHeight = isCrouching ? 1.45f : (HEIGHT - 0.1f);
        currentEyeHeight += (targetEyeHeight - currentEyeHeight) * 10f * deltaTime;

        camera.position.set(
            position.x + bobX,
            position.y + currentEyeHeight + bobY,
            position.z
        );
    }

    private void moveWithCollision(float deltaTime, World world) {
        float dx = velocity.x * deltaTime;
        float dy = velocity.y * deltaTime;
        float dz = velocity.z * deltaTime;

        if (isCrouching && onGround) {
            float step = 0.05f;

            while (dx != 0f && !isSafeToWalk(world, position.x + dx, position.z)) {
                if (Math.abs(dx) <= step) {
                    dx = 0f;
                    velocity.x = 0f;
                    break;
                }
                dx += (dx > 0f ? -step : step);
            }

            while (dz != 0f && !isSafeToWalk(world, position.x, position.z + dz)) {
                if (Math.abs(dz) <= step) {
                    dz = 0f;
                    velocity.z = 0f;
                    break;
                }
                dz += (dz > 0f ? -step : step);
            }
        }

        position.x += dx;
        if (collidesWithWorld(world)) {
            position.x -= dx;
            velocity.x = 0f;
        }

        position.z += dz;
        if (collidesWithWorld(world)) {
            position.z -= dz;
            velocity.z = 0f;
        }

        position.y += dy;
        if (collidesWithWorld(world)) {
            position.y -= dy;
            if (dy < 0f) onGround = true;
            velocity.y = 0f;
        } else {
            onGround = false;
        }
    }

    private boolean isSafeToWalk(World world, float testX, float testZ) {
        int y = (int) Math.floor(position.y - 0.1f);

        // ONLY check the CENTER of the player
        int centerX = (int) Math.floor(testX);
        int centerZ = (int) Math.floor(testZ);

        return world.getBlock(centerX, y, centerZ) != Chunk.AIR;
    }

    private boolean collidesWithWorld(World world) {
        float halfW = WIDTH / 2f;

        int x0 = (int) Math.floor(position.x - halfW);
        int x1 = (int) Math.floor(position.x + halfW);
        int y0 = (int) Math.floor(position.y);
        int y1 = (int) Math.floor(position.y + HEIGHT);
        int z0 = (int) Math.floor(position.z - halfW);
        int z1 = (int) Math.floor(position.z + halfW);

        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (world.getBlock(x, y, z) != Chunk.AIR) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public int[] findSpawnPosition(World world, int centerX, int centerZ) {
        int bestX = centerX;
        int bestZ = centerZ;
        int bestY = -1;

        for (int x = centerX - 8; x <= centerX + 8; x++) {
            for (int z = centerZ - 8; z <= centerZ + 8; z++) {
                for (int y = 255; y >= 0; y--) {
                    if (world.getBlock(x, y, z) == Chunk.GRASS &&
                        world.getBlock(x, y + 1, z) == Chunk.AIR &&
                        world.getBlock(x, y + 2, z) == Chunk.AIR) {

                        if (y > bestY) {
                            bestY = y;
                            bestX = x;
                            bestZ = z;
                        }
                        break;
                    }
                }
            }
        }

        if (bestY != -1) {
            return new int[] { bestX, bestY + 1, bestZ };
        }

        return new int[] { centerX, 100, centerZ };
    }

    public float getWalkDistance() {
        return walkDistance;
    }

    public boolean isBobbing() {
        float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        return onGround && horizontalSpeed > 0.01f;
    }

    public float getHorizontalSpeed() {
        return (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }
}