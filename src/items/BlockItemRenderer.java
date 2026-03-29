package items;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
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
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import render.Texture;

public class BlockItemRenderer {

    private int shaderProgram;
    private int vaoId;
    private int vboId;

    public void init() {
        float[] vertices = {
            // x, y, u, v, face
            // face: 0 = side texture, 1 = top texture

            // Top face
             0.0f,  0.382f,  0f, 0f, 1f,
             0.44f, 0.208f,  1f, 0f, 1f,
             0.0f,  0.012f,  1f, 1f, 1f,

             0.0f,  0.382f,  0f, 0f, 1f,
            -0.44f, 0.208f,  0f, 1f, 1f,
             0.0f,  0.012f,  1f, 1f, 1f,

            // Left side
            -0.44f, 0.208f,  0f, 1f, 0f,
             0.0f,  0.012f,  1f, 1f, 0f,
             0.0f, -0.615f,  1f, 0f, 0f,

            -0.44f, 0.208f,  0f, 1f, 0f,
             0.0f, -0.615f,  1f, 0f, 0f,
            -0.44f,-0.445f,  0f, 0f, 0f,

            // Right side
             0.44f, 0.208f,  1f, 1f, 0f,
             0.0f,  0.012f,  0f, 1f, 0f,
             0.0f, -0.615f,  0f, 0f, 0f,

             0.44f, 0.208f,  1f, 1f, 0f,
             0.0f, -0.615f,  0f, 0f, 0f,
             0.44f,-0.445f,  1f, 0f, 0f
        };

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 1, GL_FLOAT, false, 5 * Float.BYTES, 4 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);

        String vertSrc =
            "#version 330 core\n" +
            "layout(location = 0) in vec2 aPos;\n" +
            "layout(location = 1) in vec2 aUV;\n" +
            "layout(location = 2) in float aFace;\n" +
            "out vec2 uv;\n" +
            "flat out float face;\n" +
            "uniform vec2 offset;\n" +
            "uniform float scale;\n" +
            "void main() {\n" +
            "    uv = aUV;\n" +
            "    face = aFace;\n" +
            "    vec2 pos = aPos * scale + offset;\n" +
            "    gl_Position = vec4(pos, 0.0, 1.0);\n" +
            "}\n";

        String fragSrc =
            "#version 330 core\n" +
            "in vec2 uv;\n" +
            "flat in float face;\n" +
            "out vec4 FragColor;\n" +
            "uniform sampler2D texTop;\n" +
            "uniform sampler2D texSide;\n" +
            "uniform vec3 tint;\n" +
            "uniform int tintWholeBlock;\n" +
            "void main() {\n" +
            "    vec4 c;\n" +
            "    if (face > 0.5) {\n" +
            "        c = texture(texTop, uv);\n" +
            "        c.rgb *= tint;\n" +
            "    } else {\n" +
            "        c = texture(texSide, uv);\n" +
            "        if (tintWholeBlock == 1) {\n" +
            "            c.rgb *= tint;\n" +
            "        }\n" +
            "    }\n" +
            "    if (c.a < 0.1) discard;\n" +
            "    FragColor = c;\n" +
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

    public void render(float x, float y, float size, Texture top, Texture side,
                       float r, float g, float b, boolean tintWholeBlock) {
        if (top == null || side == null) return;

        glUseProgram(shaderProgram);

        glUniform2f(glGetUniformLocation(shaderProgram, "offset"), x, y);
        glUniform1f(glGetUniformLocation(shaderProgram, "scale"), size);
        glUniform3f(glGetUniformLocation(shaderProgram, "tint"), r, g, b);
        glUniform1i(glGetUniformLocation(shaderProgram, "tintWholeBlock"), tintWholeBlock ? 1 : 0);

        glUniform1i(glGetUniformLocation(shaderProgram, "texTop"), 0);
        glUniform1i(glGetUniformLocation(shaderProgram, "texSide"), 1);

        glActiveTexture(GL_TEXTURE0);
        top.bind();

        glActiveTexture(GL_TEXTURE1);
        side.bind();

        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 18);
        glBindVertexArray(0);

        // hard cleanup
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glUseProgram(0);
    }
}