package entity;
import org.joml.Vector3f;

import world.Chunk;
import world.World;

public class ItemEntity {
	public int itemId;
    public int count;

    public Vector3f position;
    public Vector3f velocity;

    public float age = 0f;
    public float pickupDelay = 0.25f;

    public ItemEntity(int itemId, int count, float x, float y, float z) {
        this.itemId = itemId;
        this.count = count;
        this.position = new Vector3f(x, y, z);
        this.velocity = new Vector3f(
            (float) ((Math.random() - 0.5) * 1.2f),
            2.2f,
            (float) ((Math.random() - 0.5) * 1.2f)
        );
    }

    public void update(float deltaTime, World world) {
        age += deltaTime;

        if (pickupDelay > 0f) {
            pickupDelay -= deltaTime;
            if (pickupDelay < 0f) {
                pickupDelay = 0f;
            }
        }

        // lighter gravity for a softer fall
        velocity.y -= 8.2f * deltaTime;

        // gentle air drag
        velocity.x *= 0.985f;
        velocity.y *= 0.985f;
        velocity.z *= 0.985f;

        float nextX = position.x + velocity.x * deltaTime;
        float nextY = position.y + velocity.y * deltaTime;
        float nextZ = position.z + velocity.z * deltaTime;

        // simple horizontal movement
        position.x = nextX;
        position.z = nextZ;

        // check for ground/block under the item
        int blockX = (int) Math.floor(position.x);
        int blockYBelow = (int) Math.floor(nextY - 0.8f);
        int blockZ = (int) Math.floor(position.z);

        boolean onSolidBlock = world.getBlock(blockX, blockYBelow, blockZ) != Chunk.AIR;

        if (onSolidBlock && velocity.y <= 0f) {
            float floorY = blockYBelow + 1.25f;

            if (nextY <= floorY) {
                position.y = floorY;

                // soft little bounce, then settle
                if (Math.abs(velocity.y) > 1.2f) {
                    velocity.y *= -0.18f;
                } else if (Math.abs(velocity.y) > 0.12f) {
                    velocity.y *= -0.08f;
                } else {
                    velocity.y = 0f;
                }

                // floor friction
                velocity.x *= 0.72f;
                velocity.z *= 0.72f;

                // kill tiny jitter
                if (Math.abs(velocity.x) < 0.02f) velocity.x = 0f;
                if (Math.abs(velocity.z) < 0.02f) velocity.z = 0f;
            } else {
                position.y = nextY;
            }
        } else {
            position.y = nextY;
        }

        // void safety
        if (position.y < -64f) {
            position.y = -64f;
            velocity.zero();
        }
    }

    public float getBobOffset() {
        return (float) Math.sin(age * 3.5f) * 0.08f;
    }

    public float getSpinAngle() {
        return age * 90f;
    }
}