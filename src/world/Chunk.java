package world;
import world.PerlinNoise;

public class Chunk {

    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 64;
    public static final int SIZE_Z = 16;

    private int[][][] blocks = new int[SIZE_X][SIZE_Y][SIZE_Z];
    private byte[][][] lightMap = new byte[SIZE_X][SIZE_Y][SIZE_Z];

    public static final int AIR = 0;
    public static final int STONE = 1;
    public static final int DIRT = 2;
    public static final int GRASS = 3;
    public static final int OAK_LOG = 4;
    public static final int OAK_LEAVES = 5;
    public static final int OAK_PLANKS = 6;
    public static final int STICK = 7;
    public static final int CRAFTING_TABLE = 8;
    public static final int COBBLESTONE = 9;
    
    
    private static PerlinNoise noise = new PerlinNoise(12345L);

    public int getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, int type) {
        blocks[x][y][z] = type;
    }

    public byte getLight(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= SIZE_Y || z < 0 || z >= SIZE_Z) return 0;
        return lightMap[x][y][z];
    }

    public void generate(int chunkX, int chunkZ) {
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                int worldX = chunkX * SIZE_X + x;
                int worldZ = chunkZ * SIZE_Z + z;
                int height = getHeight(worldX, worldZ);
                for (int y = 0; y < SIZE_Y; y++) {
                    if (y < height - 3)      blocks[x][y][z] = STONE;
                    else if (y < height)     blocks[x][y][z] = DIRT;
                    else if (y == height)    blocks[x][y][z] = GRASS;
                    else                     blocks[x][y][z] = AIR;
                }
            }
        }
        
     // Tree generation — roughly 1 tree per chunk, random position
        java.util.Random rand = new java.util.Random((long)(chunkX * 73856093) ^ (long)(chunkZ * 19349663));
        if (rand.nextInt(2) == 0) { // 50% chance per chunk
            int tx = 2 + rand.nextInt(SIZE_X - 4);
            int tz = 2 + rand.nextInt(SIZE_Z - 4);
            int ty = getHeight(chunkX * SIZE_X + tx, chunkZ * SIZE_Z + tz) + 1;
            if (ty < SIZE_Y - 8) {
                generateTree(tx, ty, tz, rand);
            }
        }
        
        calculateLight();
    }

    public void calculateLight() {
        for (int x = 0; x < SIZE_X; x++)
            for (int y = 0; y < SIZE_Y; y++)
                for (int z = 0; z < SIZE_Z; z++)
                    lightMap[x][y][z] = 0;

        java.util.Queue<int[]> queue = new java.util.LinkedList<>();

        // Seed — full sky light straight down until hitting solid
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                for (int y = SIZE_Y - 1; y >= 0; y--) {
                    if (blocks[x][y][z] != AIR) break;
                    lightMap[x][y][z] = 15;
                    queue.add(new int[]{x, y, z});
                }
            }
        }

        // Flood fill
        int[][] dirs = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0], y = pos[1], z = pos[2];
            byte current = lightMap[x][y][z];
            if (current <= 1) continue;

            for (int i = 0; i < dirs.length; i++) {
                int[] d = dirs[i];
                int nx = x + d[0], ny = y + d[1], nz = z + d[2];
                if (nx < 0 || nx >= SIZE_X || ny < 0 || ny >= SIZE_Y || nz < 0 || nz >= SIZE_Z) continue;
                if (blocks[nx][ny][nz] != AIR) continue;

                // Downward never loses light — keeps same level
                byte newLight;
                if (d[1] == -1 && current == 15) {
                    newLight = 15; // direct sky shaft — no loss
                } else {
                    newLight = (byte)(current - 1); // everything else loses 1
                }

                if (newLight > lightMap[nx][ny][nz]) {
                    lightMap[nx][ny][nz] = newLight;
                    queue.add(new int[]{nx, ny, nz});
                }
            }
        }
    }

    public static int getHeight(int x, int z) {
        float n = noise.octave(x * 0.004f, z * 0.004f, 6, 0.5f);
        int height = (int)(n * 20 + 30);

        if (height < 5) height = 5;
        if (height > SIZE_Y - 3) height = SIZE_Y - 3;

        return height;
    }
    
    
    private void generateTree(int x, int y, int z, java.util.Random rand) {
        int trunkHeight = 4 + rand.nextInt(3);

        // Trunk
        for (int i = 0; i < trunkHeight; i++) {
            if (y + i < SIZE_Y) blocks[x][y + i][z] = OAK_LOG;
        }

        int top = y + trunkHeight;

        // Layer top+1 — tiny 3x3 minus corners
        placeLeafLayer(x, top + 1, z, 1, rand, 0f);

        // Layer top — 3x3 full
        placeLeafLayer(x, top, z, 1, rand, 0f);

        // Layer top-1 — 5x5 minus corners
        placeLeafLayer(x, top - 1, z, 2, rand, 0.3f);

        // Layer top-2 — 5x5 minus corners
        placeLeafLayer(x, top - 2, z, 2, rand, 0.3f);
    }

    private void placeLeafLayer(int x, int y, int z, int radius, java.util.Random rand, float cornerChance) {
        if (y < 0 || y >= SIZE_Y) return;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Skip corners randomly
                if (Math.abs(dx) == radius && Math.abs(dz) == radius) {
                    if (rand.nextFloat() > cornerChance) continue;
                }
                setLeaf(x + dx, y, z + dz);
            }
        }
    }

    private void setLeaf(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= SIZE_Y || z < 0 || z >= SIZE_Z) return;
        if (blocks[x][y][z] == AIR) blocks[x][y][z] = OAK_LEAVES;
    }
    
    public static float getBreakTime(int block) {
        switch (block) {
            case OAK_LEAVES: return 0.75f;
            case DIRT:	     return 2.5f;
            case GRASS:      return 1.50f;
            case OAK_LOG:    return 5.25f;
            case OAK_PLANKS: return 4.5f;
            case STONE:      return 12.5f;
            case CRAFTING_TABLE: return 4.5f;
            case COBBLESTONE:   return 13.0f;
            default:         return 2.0f;
        }
    }
}