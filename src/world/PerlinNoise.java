package world;
public class PerlinNoise {

    private int[] perm = new int[512];

    public PerlinNoise(long seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;

        // Shuffle using seed
        java.util.Random rand = new java.util.Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }

        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    private float fade(float t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private float lerp(float a, float b, float t) { return a + t * (b - a); }

    private float grad(int hash, float x, float y) {
        switch (hash & 3) {
            case 0: return  x + y;
            case 1: return -x + y;
            case 2: return  x - y;
            case 3: return -x - y;
            default: return 0;
        }
    }

    public float noise(float x, float y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        float u = fade(x);
        float v = fade(y);

        int a  = perm[X]     + Y;
        int b  = perm[X + 1] + Y;

        return lerp(
            lerp(grad(perm[a],     x,     y),     grad(perm[b],     x - 1, y),     u),
            lerp(grad(perm[a + 1], x,     y - 1), grad(perm[b + 1], x - 1, y - 1), u),
            v
        );
    }

    // Multiple octaves for more natural terrain
    public float octave(float x, float y, int octaves, float persistence) {
        float total = 0, frequency = 1, amplitude = 1, maxVal = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxVal += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        return total / maxVal;
    }
}