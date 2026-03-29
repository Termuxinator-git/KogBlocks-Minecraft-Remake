package world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL11.glBindTexture;


import render.Shader;
import render.TextureManager;

public class ChunkMesh {

	private int[] vaos = new int[12];
	private int[] vbos = new int[12];
	private int[] counts = new int[12];

    static final int TEX_DIRT       = 0;
    static final int TEX_GRASS_TOP  = 1;
    static final int TEX_GRASS_SIDE = 2;
    static final int TEX_STONE      = 3;
    static final int TEX_OAK_LOG_TOP  = 4;
    static final int TEX_OAK_LOG_SIDE = 5;
    static final int TEX_OAK_LEAVES   = 6;
    static final int TEX_OAK_PLANKS   = 7;
    static final int TEX_CRAFTING_TABLE_TOP   = 8;
    static final int TEX_CRAFTING_TABLE_SIDE  = 9;
    static final int TEX_CRAFTING_TABLE_FRONT = 10;
    static final int TEX_COBBLESTONE = 11;
    
    public void build(Chunk chunk) {
        List<List<Float>> verts = new ArrayList<>();
        for (int i = 0; i < 12; i++) verts.add(new ArrayList<>());

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    int block = chunk.getBlock(x, y, z);
                    if (block == Chunk.AIR) continue;

                    if (isAir(chunk, x, y + 1, z)) addFace(verts, x, y, z, 0, block, sampleLight(chunk, x, y + 1, z));
                    if (isAir(chunk, x, y - 1, z)) addFace(verts, x, y, z, 1, block, sampleLight(chunk, x, y - 1, z));
                    if (isAir(chunk, x + 1, y, z)) addFace(verts, x, y, z, 2, block, sampleLight(chunk, x + 1, y, z));
                    if (isAir(chunk, x - 1, y, z)) addFace(verts, x, y, z, 3, block, sampleLight(chunk, x - 1, y, z));
                    if (isAir(chunk, x, y, z + 1)) addFace(verts, x, y, z, 4, block, sampleLight(chunk, x, y, z + 1));
                    if (isAir(chunk, x, y, z - 1)) addFace(verts, x, y, z, 5, block, sampleLight(chunk, x, y, z - 1));
                }
            }
        }

        vaos = new int[12];
        vbos = new int[12];
        glGenVertexArrays(vaos);
        glGenBuffers(vbos);

        for (int t = 0; t < 12; t++) {
            List<Float> v = verts.get(t);
            float[] data = new float[v.size()];
            for (int i = 0; i < data.length; i++) data[i] = v.get(i);
            counts[t] = data.length / 9;

            glBindVertexArray(vaos[t]);
            glBindBuffer(GL_ARRAY_BUFFER, vbos[t]);
            glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8 * Float.BYTES);
            glEnableVertexAttribArray(3);

            glBindVertexArray(0);
        }
    }

    // Safe light sampler — returns full light if out of bounds (open sky)
    private float sampleLight(Chunk chunk, int x, int y, int z) {
        if (y >= Chunk.SIZE_Y) return 1f;
        if (y < 0) return 0f;
        // Out of chunk bounds horizontally — assume full sky light
        if (x < 0 || x >= Chunk.SIZE_X || z < 0 || z >= Chunk.SIZE_Z) return 1f;
        return chunk.getLight(x, y, z) / 15f;
    }

    private boolean isAir(Chunk chunk, int x, int y, int z) {
        if (x < 0 || x >= Chunk.SIZE_X || y < 0 || y >= Chunk.SIZE_Y || z < 0 || z >= Chunk.SIZE_Z)
            return true;
        int block = chunk.getBlock(x, y, z);
        return block == Chunk.AIR || block == Chunk.OAK_LEAVES;
    }

    private int getTexIndex(int block, int face) {
        switch (block) {
            
        case Chunk.GRASS:
                if (face == 0) return TEX_GRASS_TOP;
                if (face == 1) return TEX_DIRT;
                return TEX_GRASS_SIDE;
            
            case Chunk.DIRT:      return TEX_DIRT;
            
            case Chunk.STONE:     return TEX_STONE;
            case Chunk.COBBLESTONE: return TEX_COBBLESTONE;
            
            case Chunk.OAK_LOG:
                if (face == 0 || face == 1) return TEX_OAK_LOG_TOP;
                return TEX_OAK_LOG_SIDE;
            
            case Chunk.OAK_LEAVES: return TEX_OAK_LEAVES;
            
            case Chunk.OAK_PLANKS: return TEX_OAK_PLANKS;
            
            default: return TEX_DIRT;
            
            case Chunk.CRAFTING_TABLE:
                if (face == 0) return TEX_CRAFTING_TABLE_TOP;
                if (face == 1) return TEX_OAK_PLANKS;
                if (face == 5) return TEX_CRAFTING_TABLE_FRONT;
                return TEX_CRAFTING_TABLE_SIDE;
        }
    }
    private void addFace(List<List<Float>> verts, int x, int y, int z, int face, int block, float light) {
        int texIdx = getTexIndex(block, face);
        List<Float> v = verts.get(texIdx);

        float x0 = x, y0 = y, z0 = z;
        float x1 = x + 1, y1 = y + 1, z1 = z + 1;

        switch (face) {
            case 0 -> {
                v.addAll(Arrays.asList(
                    x0,y1,z0, 0f,1f,0f, 0f,0f, light,
                    x1,y1,z0, 0f,1f,0f, 1f,0f, light,
                    x1,y1,z1, 0f,1f,0f, 1f,1f, light,
                    x1,y1,z1, 0f,1f,0f, 1f,1f, light,
                    x0,y1,z1, 0f,1f,0f, 0f,1f, light,
                    x0,y1,z0, 0f,1f,0f, 0f,0f, light
                ));
            }
            case 1 -> {
                v.addAll(Arrays.asList(
                    x0,y0,z0, 0f,-1f,0f, 0f,0f, light,
                    x1,y0,z1, 0f,-1f,0f, 1f,1f, light,
                    x1,y0,z0, 0f,-1f,0f, 1f,0f, light,
                    x1,y0,z1, 0f,-1f,0f, 1f,1f, light,
                    x0,y0,z0, 0f,-1f,0f, 0f,0f, light,
                    x0,y0,z1, 0f,-1f,0f, 0f,1f, light
                ));
            }
            case 2 -> {
                v.addAll(Arrays.asList(
                    x1,y0,z0, 1f,0f,0f, 0f,0f, light,
                    x1,y1,z0, 1f,0f,0f, 0f,1f, light,
                    x1,y1,z1, 1f,0f,0f, 1f,1f, light,
                    x1,y1,z1, 1f,0f,0f, 1f,1f, light,
                    x1,y0,z1, 1f,0f,0f, 1f,0f, light,
                    x1,y0,z0, 1f,0f,0f, 0f,0f, light
                ));
            }
            case 3 -> {
                v.addAll(Arrays.asList(
                    x0,y0,z0, -1f,0f,0f, 1f,0f, light,
                    x0,y1,z1, -1f,0f,0f, 0f,1f, light,
                    x0,y1,z0, -1f,0f,0f, 1f,1f, light,
                    x0,y1,z1, -1f,0f,0f, 0f,1f, light,
                    x0,y0,z0, -1f,0f,0f, 1f,0f, light,
                    x0,y0,z1, -1f,0f,0f, 0f,0f, light
                ));
            }
            case 4 -> {
                v.addAll(Arrays.asList(
                    x0,y0,z1, 0f,0f,1f, 0f,0f, light,
                    x1,y0,z1, 0f,0f,1f, 1f,0f, light,
                    x1,y1,z1, 0f,0f,1f, 1f,1f, light,
                    x1,y1,z1, 0f,0f,1f, 1f,1f, light,
                    x0,y1,z1, 0f,0f,1f, 0f,1f, light,
                    x0,y0,z1, 0f,0f,1f, 0f,0f, light
                ));
            }
            case 5 -> {
                v.addAll(Arrays.asList(
                    x0,y0,z0, 0f,0f,-1f, 1f,0f, light,
                    x1,y1,z0, 0f,0f,-1f, 0f,1f, light,
                    x1,y0,z0, 0f,0f,-1f, 0f,0f, light,
                    x1,y1,z0, 0f,0f,-1f, 0f,1f, light,
                    x0,y0,z0, 0f,0f,-1f, 1f,0f, light,
                    x0,y1,z0, 0f,0f,-1f, 1f,1f, light
                ));
            }
        }
    }

    public void render(int chunkX, int chunkZ, Shader shader) {
        org.joml.Matrix4f model = new org.joml.Matrix4f().translation(
            chunkX * Chunk.SIZE_X, 0, chunkZ * Chunk.SIZE_Z
        );
        shader.setMatrix4f("model", model);

        glActiveTexture(GL_TEXTURE0);

        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.dirt.bind();
        glBindVertexArray(vaos[TEX_DIRT]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_DIRT]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(0.52f, 0.82f, 0.25f));
        TextureManager.grassTop.bind();
        glBindVertexArray(vaos[TEX_GRASS_TOP]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_GRASS_TOP]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.grassSide.bind();
        glBindVertexArray(vaos[TEX_GRASS_SIDE]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_GRASS_SIDE]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.stone.bind();
        glBindVertexArray(vaos[TEX_STONE]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_STONE]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.cobblestone.bind();
        glBindVertexArray(vaos[TEX_COBBLESTONE]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_COBBLESTONE]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.oakLogTop.bind();
        glBindVertexArray(vaos[TEX_OAK_LOG_TOP]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_OAK_LOG_TOP]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.oakLogSide.bind();
        glBindVertexArray(vaos[TEX_OAK_LOG_SIDE]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_OAK_LOG_SIDE]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.oakPlanks.bind();
        glBindVertexArray(vaos[TEX_OAK_PLANKS]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_OAK_PLANKS]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(0.42f, 0.67f, 0.20f));
        TextureManager.oakLeaves.bind();
        glBindVertexArray(vaos[TEX_OAK_LEAVES]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_OAK_LEAVES]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.craftingTableTop.bind();
        glBindVertexArray(vaos[TEX_CRAFTING_TABLE_TOP]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_CRAFTING_TABLE_TOP]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.craftingTableSide.bind();
        glBindVertexArray(vaos[TEX_CRAFTING_TABLE_SIDE]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_CRAFTING_TABLE_SIDE]);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(1f, 1f, 1f));
        TextureManager.craftingTableFront.bind();
        glBindVertexArray(vaos[TEX_CRAFTING_TABLE_FRONT]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_CRAFTING_TABLE_FRONT]);
    
        glBindVertexArray(0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
        
    }
    

    public void renderTransparent(int chunkX, int chunkZ, Shader shader) {
        org.joml.Matrix4f model = new org.joml.Matrix4f().translation(
            chunkX * Chunk.SIZE_X, 0, chunkZ * Chunk.SIZE_Z
        );
        shader.setMatrix4f("model", model);

        glActiveTexture(GL_TEXTURE0);
        shader.setVec3("tint", new org.joml.Vector3f(0.42f, 0.67f, 0.20f));
        TextureManager.oakLeaves.bind();
        glBindVertexArray(vaos[TEX_OAK_LEAVES]);
        glDrawArrays(GL_TRIANGLES, 0, counts[TEX_OAK_LEAVES]);

        glBindVertexArray(0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}