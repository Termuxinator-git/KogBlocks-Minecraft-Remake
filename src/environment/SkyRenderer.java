package environment;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
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
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import render.Texture;

public class SkyRenderer {

    private int vaoId, vboId;
    private int shaderProgram;
    private Texture sunTex, moonTex;

    private static final float SKY_RADIUS = 200f;
    private static final float BODY_SIZE  = 40f;

    public void init() {
        sunTex  = new Texture("textures/environment/sun.png");
        moonTex = new Texture("textures/environment/moon.png");

        float h = BODY_SIZE / 2f;
        float[] verts = {
            -h, -h, 0,  0, 0,
             h, -h, 0,  1, 0,
             h,  h, 0,  1, 1,
             h,  h, 0,  1, 1,
            -h,  h, 0,  0, 1,
            -h, -h, 0,  0, 0,
        };

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
            "uniform mat4 mvp;\n" +
            "out vec2 uv;\n" +
            "void main() { uv = aUV; gl_Position = mvp * vec4(aPos, 1.0); }\n";

        String fragSrc =
            "#version 330 core\n" +
            "in vec2 uv;\n" +
            "out vec4 FragColor;\n" +
            "uniform sampler2D tex;\n" +
            "uniform bool isGlow;\n" +
            "void main() {\n" +
            "    if (isGlow) {\n" +
            "        vec2 center = uv - 0.5;\n" +
            "        float dist = length(center) * 2.0;\n" +
            "        float alpha = pow(max(0.0, 1.0 - dist), 0.9);\n" +
            "        FragColor = vec4(1.0, 0.95, 0.7, alpha * 0.15);\n" +
            "    } else {\n" +
            "        vec4 c = texture(tex, uv);\n" +
            "        if (c.a < 0.1) discard;\n" +
            "        FragColor = c;\n" +
            "    }\n" +
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

    private void renderBody(Texture tex, Vector3f playerPos, Vector3f dir,
                            Matrix4f view, Matrix4f projection) {
        Vector3f pos = new Vector3f(playerPos).add(new Vector3f(dir).mul(SKY_RADIUS));

        Vector3f forward = new Vector3f(dir).negate().normalize();
        Vector3f worldUp = new Vector3f(0, 1, 0);
        Vector3f right   = new Vector3f(worldUp).cross(forward).normalize();
        Vector3f up      = new Vector3f(forward).cross(right).normalize();

        Matrix4f model = new Matrix4f(
            right.x,   right.y,   right.z,   0,
            up.x,      up.y,      up.z,      0,
            forward.x, forward.y, forward.z, 0,
            pos.x,     pos.y,     pos.z,     1
        );

        Matrix4f mvp = new Matrix4f(projection).mul(view).mul(model);
        float[] buf = new float[16];
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "mvp"), false, mvp.get(buf));
        glUniform1i(glGetUniformLocation(shaderProgram, "isGlow"), 0);

        tex.bind();
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    private void renderGlow(Vector3f playerPos, Vector3f dir,
                            Matrix4f view, Matrix4f projection, float scale) {
        Vector3f pos = new Vector3f(playerPos).add(new Vector3f(dir).mul(SKY_RADIUS + 1f));

        Vector3f forward = new Vector3f(dir).negate().normalize();
        Vector3f worldUp = new Vector3f(0, 1, 0);
        Vector3f right   = new Vector3f(worldUp).cross(forward).normalize();
        Vector3f up      = new Vector3f(forward).cross(right).normalize();

        Matrix4f model = new Matrix4f(
            right.x,   right.y,   right.z,   0,
            up.x,      up.y,      up.z,      0,
            forward.x, forward.y, forward.z, 0,
            pos.x,     pos.y,     pos.z,     1
        ).scale(scale);

        Matrix4f mvp = new Matrix4f(projection).mul(view).mul(model);
        float[] buf = new float[16];
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "mvp"), false, mvp.get(buf));
        glUniform1i(glGetUniformLocation(shaderProgram, "isGlow"), 1);

        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    public void render(Vector3f playerPos, DayNightCycle dayNight,
                       Matrix4f view, Matrix4f projection) {
        glDepthMask(false);
        glEnable(GL_BLEND);
        glUseProgram(shaderProgram);

        Vector3f sunDir  = dayNight.getLightDir();
        Vector3f moonDir = new Vector3f(sunDir).negate();

        // Sun glow
        if (dayNight.isDay()) {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            renderGlow(playerPos, sunDir, view, projection, 6f);
            renderGlow(playerPos, sunDir, view, projection, 12f);
            renderGlow(playerPos, sunDir, view, projection, 20f);
        }

        // Moon glow
        if (!dayNight.isDay()) {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            renderGlow(playerPos, moonDir, view, projection, 4f);
            renderGlow(playerPos, moonDir, view, projection, 8f);
        }

        // Actual textures
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        renderBody(sunTex, playerPos, sunDir, view, projection);
        renderBody(moonTex, playerPos, moonDir, view, projection);

        glDisable(GL_BLEND);
        glDepthMask(true);
    }
}