package render;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.joml.Vector3f;

public class Shader {

    public int programId;

    public Shader(String vertexPath, String fragmentPath) {
        String vertSrc = load(vertexPath);
        String fragSrc = load(fragmentPath);

        int vert = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vert, vertSrc);
        glCompileShader(vert);
        if (glGetShaderi(vert, GL_COMPILE_STATUS) == 0)
            System.err.println("Vertex shader error: " + glGetShaderInfoLog(vert));

        int frag = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(frag, fragSrc);
        glCompileShader(frag);
        if (glGetShaderi(frag, GL_COMPILE_STATUS) == 0)
            System.err.println("Fragment shader error: " + glGetShaderInfoLog(frag));

        programId = glCreateProgram();
        glAttachShader(programId, vert);
        glAttachShader(programId, frag);
        glLinkProgram(programId);

        glDeleteShader(vert);
        glDeleteShader(frag);
    }

    public void use() { glUseProgram(programId); }

    public void setVec3(String name, Vector3f v) {
        glUniform3f(glGetUniformLocation(programId, name), v.x, v.y, v.z);
    }

    public void setFloat(String name, float value) {
        glUniform1f(glGetUniformLocation(programId, name), value);
    }

    public void setMatrix4f(String name, org.joml.Matrix4f mat) {
        float[] buf = new float[16];
        mat.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(programId, name), false, buf);
    }

    private String load(String path) {
        try { return new String(Files.readAllBytes(Paths.get(path))); }
        catch (IOException e) { throw new RuntimeException("Shader not found: " + path); }
    }
    
    public int getProgramId() {
        return programId;
    }
}