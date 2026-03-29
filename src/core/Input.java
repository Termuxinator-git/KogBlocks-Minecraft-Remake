package core;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.lwjgl.glfw.GLFW;

public class Input {
    private static boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private static boolean[] keyJustPressed = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private static boolean[] mouseButtons = new boolean[8];
    private static boolean[] mouseJustPressed = new boolean[8];

    private static double mouseX, mouseY;
    private static double deltaX, deltaY;
    private static double lastX, lastY;
    private static double scrollDelta = 0;

    private static final StringBuilder typedChars = new StringBuilder();
    private static boolean textInputEnabled = false;

    public static void init(long window) {
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key >= 0 && key < keys.length) {
                keyJustPressed[key] = (action == GLFW_PRESS) && !keys[key];
                keys[key] = (action != GLFW_RELEASE);
            }
        });

        glfwSetCharCallback(window, (win, codepoint) -> {
            if (!textInputEnabled) return;

            if (codepoint >= 32 && codepoint != 127) {
                typedChars.append((char) codepoint);
            }
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button >= 0 && button < mouseButtons.length) {
                mouseJustPressed[button] = (action == GLFW_PRESS) && !mouseButtons[button];
                mouseButtons[button] = (action != GLFW_RELEASE);
            }
        });

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            deltaX = xpos - lastX;
            deltaY = ypos - lastY;
            lastX = xpos;
            lastY = ypos;
            mouseX = xpos;
            mouseY = ypos;
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            scrollDelta = yoffset;
        });
    }

    public static void beginTextInput() {
        textInputEnabled = true;
        typedChars.setLength(0);
    }

    public static void endTextInput() {
        textInputEnabled = false;
        typedChars.setLength(0);
    }

    public static boolean isKeyDown(int key) {
        return key >= 0 && key < keys.length && keys[key];
    }

    public static boolean isKeyJustPressed(int key) {
        return key >= 0 && key < keyJustPressed.length && keyJustPressed[key];
    }

    public static boolean isMouseDown(int button) {
        return mouseButtons[button];
    }

    public static boolean isMouseJustPressed(int button) {
        return mouseJustPressed[button];
    }

    public static String consumeTypedChars() {
        String out = typedChars.toString();
        typedChars.setLength(0);
        return out;
    }

    public static void resetMouseJustPressed() {
        for (int i = 0; i < mouseJustPressed.length; i++) {
            mouseJustPressed[i] = false;
        }
    }

    public static void resetKeyJustPressed() {
        for (int i = 0; i < keyJustPressed.length; i++) {
            keyJustPressed[i] = false;
        }
    }

    public static double getDeltaX() {
        return deltaX;
    }

    public static double getDeltaY() {
        return deltaY;
    }

    public static void resetDeltas() {
        deltaX = 0;
        deltaY = 0;
    }

    public static double getMouseX() {
        return mouseX;
    }

    public static double getMouseY() {
        return mouseY;
    }

    public static double getScrollDelta() {
        return scrollDelta;
    }

    public static void resetScroll() {
        scrollDelta = 0;
    }
}