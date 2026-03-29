package world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entity.ItemEntity;
import render.Shader;

public class World {

    private HashMap<Long, Chunk> chunks = new HashMap<>();
    private HashMap<Long, ChunkMesh> meshes = new HashMap<>();

    private static final int RENDER_DISTANCE = 8;

    private long key(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    public void update(float playerX, float playerZ) {
        int pcx = (int) Math.floor(playerX / Chunk.SIZE_X);
        int pcz = (int) Math.floor(playerZ / Chunk.SIZE_Z);

        for (int cx = pcx - RENDER_DISTANCE; cx <= pcx + RENDER_DISTANCE; cx++) {
            for (int cz = pcz - RENDER_DISTANCE; cz <= pcz + RENDER_DISTANCE; cz++) {
                long k = key(cx, cz);
                if (!chunks.containsKey(k)) {
                    Chunk c = new Chunk();
                    c.generate(cx, cz);
                    ChunkMesh m = new ChunkMesh();
                    m.build(c);
                    chunks.put(k, c);
                    meshes.put(k, m);
                }
            }
        }

        List<Long> toRemove = new ArrayList<>();
        for (long k : chunks.keySet()) {
            int cx = (int)(k >> 32);
            int cz = (int)(k & 0xFFFFFFFFL);
            if (Math.abs(cx - pcx) > RENDER_DISTANCE + 1 ||
                Math.abs(cz - pcz) > RENDER_DISTANCE + 1) {
                toRemove.add(k);
            }
        }
        for (long k : toRemove) {
            chunks.remove(k);
            meshes.remove(k);
        }
    }

    public int getBlock(int x, int y, int z) {
        int cx = (int) Math.floor((float) x / Chunk.SIZE_X);
        int cz = (int) Math.floor((float) z / Chunk.SIZE_Z);
        long k = key(cx, cz);
        Chunk c = chunks.get(k);
        if (c == null) return Chunk.AIR;

        int lx = x - cx * Chunk.SIZE_X;
        int lz = z - cz * Chunk.SIZE_Z;
        if (y < 0 || y >= Chunk.SIZE_Y) return Chunk.AIR;
        return c.getBlock(lx, y, lz);
    }

    public void setBlock(int x, int y, int z, int type) {
        int cx = (int) Math.floor((float) x / Chunk.SIZE_X);
        int cz = (int) Math.floor((float) z / Chunk.SIZE_Z);
        long k = key(cx, cz);
        Chunk c = chunks.get(k);
        if (c == null) return;

        int lx = x - cx * Chunk.SIZE_X;
        int lz = z - cz * Chunk.SIZE_Z;
        if (y < 0 || y >= Chunk.SIZE_Y) return;
        c.setBlock(lx, y, lz, type);

        recalcLightAround(cx, cz);
    }

    private void recalcLightAround(int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long k = key(cx + dx, cz + dz);
                Chunk c = chunks.get(k);
                if (c != null) c.calculateLight();
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long k = key(cx + dx, cz + dz);
                Chunk c = chunks.get(k);
                if (c != null) {
                    ChunkMesh m = new ChunkMesh();
                    m.build(c);
                    meshes.put(k, m);
                }
            }
        }
    }

    public int[] raycast(org.joml.Vector3f origin, org.joml.Vector3f dir, float maxDist) {
        float x = origin.x, y = origin.y, z = origin.z;
        float dx = dir.x, dy = dir.y, dz = dir.z;

        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;
        int stepZ = dz > 0 ? 1 : -1;

        int bx = (int) Math.floor(x);
        int by = (int) Math.floor(y);
        int bz = (int) Math.floor(z);

        float tDeltaX = Math.abs(1f / dx);
        float tDeltaY = Math.abs(1f / dy);
        float tDeltaZ = Math.abs(1f / dz);

        float tMaxX = (dx > 0 ? (bx + 1 - x) : (x - bx)) * tDeltaX;
        float tMaxY = (dy > 0 ? (by + 1 - y) : (y - by)) * tDeltaY;
        float tMaxZ = (dz > 0 ? (bz + 1 - z) : (z - bz)) * tDeltaZ;

        int lastFaceX = 0, lastFaceY = 0, lastFaceZ = 0;
        float dist = 0;

        while (dist < maxDist) {
            if (getBlock(bx, by, bz) != Chunk.AIR)
                return new int[]{bx, by, bz, lastFaceX, lastFaceY, lastFaceZ};

            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                lastFaceX = -stepX; lastFaceY = 0; lastFaceZ = 0;
                bx += stepX; dist = tMaxX; tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                lastFaceX = 0; lastFaceY = -stepY; lastFaceZ = 0;
                by += stepY; dist = tMaxY; tMaxY += tDeltaY;
            } else {
                lastFaceX = 0; lastFaceY = 0; lastFaceZ = -stepZ;
                bz += stepZ; dist = tMaxZ; tMaxZ += tDeltaZ;
            }
        }
        return null;
    }

    public void render(Shader shader) {
        for (long k : meshes.keySet()) {
            int cx = (int)(k >> 32);
            int cz = (int)(k & 0xFFFFFFFFL);
            meshes.get(k).render(cx, cz, shader);
        }
    }
    
    private final List<ItemEntity> itemEntities = new ArrayList<>();
    
    public void spawnItem(int itemId, int count, float x, float y, float z) {
        itemEntities.add(new ItemEntity(itemId, count, x, y, z));
    }

    public List<ItemEntity> getItemEntities() {
        return itemEntities;
    }
    
    public void updateItems(float deltaTime) {
        for (ItemEntity item : itemEntities) {
            item.update(deltaTime, this);
        }
    }
    
}