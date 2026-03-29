package blocks;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
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

import controls.Camera;

public class BlockOutline {

    private int vaoId, vboId;
    private int shaderProgram;

    public void init() {
        float o = 0.001f; // tiny offset so it doesn't z-fight
        float[] vertices = {
            // Bottom face
            -o,-o,-o,  1+o,-o,-o,
            1+o,-o,-o,  1+o,-o,1+o,
            1+o,-o,1+o,  -o,-o,1+o,
            -o,-o,1+o,  -o,-o,-o,
            // Top face
            -o,1+o,-o,  1+o,1+o,-o,
            1+o,1+o,-o,  1+o,1+o,1+o,
            1+o,1+o,1+o,  -o,1+o,1+o,
            -o,1+o,1+o,  -o,1+o,-o,
            // Vertical edges
            -o,-o,-o,  -o,1+o,-o,
            1+o,-o,-o,  1+o,1+o,-o,
            1+o,-o,1+o,  1+o,1+o,1+o,
            -o,-o,1+o,  -o,1+o,1+o,
        };

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        String vertSrc =
            "#version 330 core\n" +
            "layout(location = 0) in vec3 aPos;\n" +
            "uniform mat4 model;\n" +
            "uniform mat4 view;\n" +
            "uniform mat4 projection;\n" +
            "void main() { gl_Position = projection * view * model * vec4(aPos, 1.0); }\n";

        String fragSrc =
            "#version 330 core\n" +
            "out vec4 FragColor;\n" +
            "void main() { FragColor = vec4(0.0, 0.0, 0.0, 1.0); }\n";

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

    public void render(int[] block, Camera camera, org.joml.Matrix4f projection) {
        if (block == null) return;

        org.joml.Matrix4f model = new org.joml.Matrix4f().translation(block[0], block[1], block[2]);

        glUseProgram(shaderProgram);

        int modelLoc = glGetUniformLocation(shaderProgram, "model");
        int viewLoc  = glGetUniformLocation(shaderProgram, "view");
        int projLoc  = glGetUniformLocation(shaderProgram, "projection");

        float[] buf = new float[16];
        glUniformMatrix4fv(modelLoc, false, model.get(buf));
        glUniformMatrix4fv(viewLoc,  false, camera.getViewMatrix().get(buf));
        glUniformMatrix4fv(projLoc,  false, projection.get(buf));

        glBindVertexArray(vaoId);
        glDrawArrays(GL_LINES, 0, 24);
        glBindVertexArray(0);
    }
}