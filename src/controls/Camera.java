package controls;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Input;

public class Camera {
    public Vector3f position = new Vector3f(0, 20, 0);
    public float yaw = -90f;
    public float pitch = 0f;
    public float roll = 0f; // for head bob tilt

    private Vector3f front = new Vector3f(0, 0, -1);
    private Vector3f up = new Vector3f(0, 1, 0);

    float sensitivity = 0.1f;

    public void update() {
        yaw   += (float)(Input.getDeltaX() * sensitivity);
        pitch -= (float)(Input.getDeltaY() * sensitivity);
        if (pitch >  89f) pitch =  89f;
        if (pitch < -89f) pitch = -89f;

        front.x = (float)(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float)(Math.sin(Math.toRadians(pitch)));
        front.z = (float)(Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.normalize();

        Input.resetDeltas();
    }

    public Matrix4f getViewMatrix() {
        // Apply roll by rotating the up vector
        Vector3f right = new Vector3f(front).cross(up).normalize();
        Vector3f tiltedUp = new Vector3f(up)
            .add(new Vector3f(right).mul((float) Math.sin(Math.toRadians(roll))));
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), tiltedUp);
    }

    public Vector3f getFront() {
        return new Vector3f(front);
    }



}