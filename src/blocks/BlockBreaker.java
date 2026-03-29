package blocks;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
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
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.joml.Matrix4f;

import controls.Camera;
import core.Input;
import render.Texture;
import world.Chunk;
import world.World;

public class BlockBreaker {

    private float breakProgress = 0f;
    private int[] currentTarget = null;

    private int vaoId;
    private int vboId;
    private int shaderProgram;
    private final Texture[] crackTextures = new Texture[10];

    public void init() {
        for (int i = 0; i < 10; i++) {
            crackTextures[i] = new Texture("textures/environment/destroy_stage_" + (i + 1) + ".png");
        }

        float o = 0.001f;
        float[] verts = buildCubeVerts(o);

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);

        String vertSrc =
            "#version 330 core\n" +
            "layout(location = 0) in vec3 aPos;\n" +
            "layout(location = 1) in vec2 aUV;\n" +
            "uniform mat4 model;\n" +
            "uniform mat4 view;\n" +
            "uniform mat4 projection;\n" +
            "out vec2 uv;\n" +
            "void main() {\n" +
            "    uv = aUV;\n" +
            "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
            "}\n";

        String fragSrc =
            "#version 330 core\n" +
            "in vec2 uv;\n" +
            "out vec4 FragColor;\n" +
            "uniform sampler2D tex;\n" +
            "void main() {\n" +
            "    vec4 c = texture(tex, uv);\n" +
            "    if (c.a < 0.05) discard;\n" +
            "    FragColor = vec4(c.rgb, c.a * 0.8);\n" +
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
    }

    private float[] buildCubeVerts(float o) {
        float x0 = -o, y0 = -o, z0 = -o;
        float x1 = 1 + o, y1 = 1 + o, z1 = 1 + o;

        return new float[] {
            // Top
            x0, y1, z0, 0, 0,   x1, y1, z0, 1, 0,   x1, y1, z1, 1, 1,
            x1, y1, z1, 1, 1,   x0, y1, z1, 0, 1,   x0, y1, z0, 0, 0,

            // Bottom
            x0, y0, z0, 0, 0,   x1, y0, z1, 1, 1,   x1, y0, z0, 1, 0,
            x1, y0, z1, 1, 1,   x0, y0, z0, 0, 0,   x0, y0, z1, 0, 1,

            // Right
            x1, y0, z0, 0, 0,   x1, y1, z0, 0, 1,   x1, y1, z1, 1, 1,
            x1, y1, z1, 1, 1,   x1, y0, z1, 1, 0,   x1, y0, z0, 0, 0,

            // Left
            x0, y0, z0, 1, 0,   x0, y1, z1, 0, 1,   x0, y1, z0, 1, 1,
            x0, y1, z1, 0, 1,   x0, y0, z0, 1, 0,   x0, y0, z1, 0, 0,

            // Front
            x0, y0, z1, 0, 0,   x1, y0, z1, 1, 0,   x1, y1, z1, 1, 1,
            x1, y1, z1, 1, 1,   x0, y1, z1, 0, 1,   x0, y0, z1, 0, 0,

            // Back
            x0, y0, z0, 1, 0,   x1, y1, z0, 0, 1,   x1, y0, z0, 0, 0,
            x1, y1, z0, 0, 1,   x0, y0, z0, 1, 0,   x0, y1, z0, 1, 1
        };
    }

    public boolean update(float deltaTime, int[] targeted, World world) {
        if (!Input.isMouseDown(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            breakProgress = 0f;
            currentTarget = null;
            return false;
        }

        if (targeted == null) {
            breakProgress = 0f;
            currentTarget = null;
            return false;
        }

        if (currentTarget == null ||
            currentTarget[0] != targeted[0] ||
            currentTarget[1] != targeted[1] ||
            currentTarget[2] != targeted[2]) {
            breakProgress = 0f;
            currentTarget = new int[] { targeted[0], targeted[1], targeted[2] };
        }

        int block = world.getBlock(targeted[0], targeted[1], targeted[2]);
        float breakTime = Chunk.getBreakTime(block);
        breakProgress += (deltaTime * 2f) / breakTime;

        if (breakProgress >= 1f) {
            breakProgress = 0f;
            currentTarget = null;
            return true;
        }

        return false;
    }

    public void render(int[] targeted, Camera camera, Matrix4f projection) {
        if (targeted == null || currentTarget == null || breakProgress <= 0f) {
            return;
        }

        int stage = (int) (breakProgress * 10f);
        if (stage < 0) stage = 0;
        if (stage > 9) stage = 9;

        Matrix4f model = new Matrix4f().translation(targeted[0], targeted[1], targeted[2]);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        glUseProgram(shaderProgram);

        float[] modelBuf = new float[16];
        float[] viewBuf = new float[16];
        float[] projBuf = new float[16];

        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, model.get(modelBuf));
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "view"), false, camera.getViewMatrix().get(viewBuf));
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "projection"), false, projection.get(projBuf));

        glActiveTexture(GL_TEXTURE0);
        crackTextures[stage].bind();
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 36);

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glColor4f(1f, 1f, 1f, 1f);
    }

    public boolean isBreaking() {
        return currentTarget != null &&
               Input.isMouseDown(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }
}