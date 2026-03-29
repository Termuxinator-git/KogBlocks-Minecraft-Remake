package gui;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import core.Input;
import render.FontRenderer;
import render.TextQuadRenderer;

public class ChatOverlay {

    private static final float H = 768f;

    private static final double MESSAGE_LIFETIME = 5.0;
    private static final int MAX_MESSAGES = 8;

    private static final double BACKSPACE_INITIAL_DELAY = 0.38;
    private static final double BACKSPACE_REPEAT_RATE = 0.045;

    private final List<ChatMessage> messages = new ArrayList<>();
    private final StringBuilder currentInput = new StringBuilder();

    private final List<String> inputHistory = new ArrayList<>();
    private int historyIndex = -1;
    private String historyDraft = "";

    private int caretPos = 0;
    private double caretBlinkTime = 0.0;

    private boolean open = false;

    private double backspaceHoldTime = 0.0;
    private double nextBackspaceRepeat = 0.0;

    public void open() {
        open = true;
        currentInput.setLength(0);
        backspaceHoldTime = 0.0;
        nextBackspaceRepeat = 0.0;
        historyIndex = -1;
        historyDraft = "";
        caretPos = 0;
        caretBlinkTime = 0.0;
        Input.beginTextInput();
    }

    public void close() {
        open = false;
        backspaceHoldTime = 0.0;
        nextBackspaceRepeat = 0.0;
        caretPos = 0;
        Input.endTextInput();
    }

    public boolean isOpen() {
        return open;
    }

    public String pollSubmittedText(double now, double deltaTime) {
        if (!open) return null;

        caretBlinkTime += deltaTime;

        String typed = Input.consumeTypedChars();
        if (!typed.isEmpty()) {
            currentInput.insert(caretPos, typed);
            caretPos += typed.length();
        }

        boolean ctrlDown = Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) || Input.isKeyDown(GLFW_KEY_RIGHT_CONTROL);

        if (Input.isKeyJustPressed(GLFW_KEY_LEFT) && caretPos > 0) {
            caretPos--;
        }

        if (Input.isKeyJustPressed(GLFW_KEY_RIGHT) && caretPos < currentInput.length()) {
            caretPos++;
        }

        if (Input.isKeyJustPressed(GLFW_KEY_UP)) {
            if (!inputHistory.isEmpty()) {
                if (historyIndex == -1) {
                    historyDraft = currentInput.toString();
                    historyIndex = inputHistory.size() - 1;
                } else if (historyIndex > 0) {
                    historyIndex--;
                }

                currentInput.setLength(0);
                currentInput.append(inputHistory.get(historyIndex));
                caretPos = currentInput.length();
            }
        }

        if (Input.isKeyJustPressed(GLFW_KEY_DOWN)) {
            if (historyIndex != -1) {
                if (historyIndex < inputHistory.size() - 1) {
                    historyIndex++;
                    currentInput.setLength(0);
                    currentInput.append(inputHistory.get(historyIndex));
                } else {
                    historyIndex = -1;
                    currentInput.setLength(0);
                    currentInput.append(historyDraft);
                }
                caretPos = currentInput.length();
            }
        }

        if (Input.isKeyJustPressed(GLFW_KEY_BACKSPACE)) {
            if (ctrlDown) {
                deletePreviousWord();
            } else if (caretPos > 0) {
                currentInput.deleteCharAt(caretPos - 1);
                caretPos--;
            }

            backspaceHoldTime = 0.0;
            nextBackspaceRepeat = BACKSPACE_INITIAL_DELAY;
        } else if (Input.isKeyDown(GLFW_KEY_BACKSPACE)) {
            backspaceHoldTime += deltaTime;

            while (backspaceHoldTime >= nextBackspaceRepeat) {
                if (ctrlDown) {
                    if (caretPos <= 0) break;
                    deletePreviousWord();
                } else {
                    if (caretPos <= 0) break;
                    currentInput.deleteCharAt(caretPos - 1);
                    caretPos--;
                }

                nextBackspaceRepeat += BACKSPACE_REPEAT_RATE;
            }
        } else {
            backspaceHoldTime = 0.0;
            nextBackspaceRepeat = 0.0;
        }

        if (Input.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            close();
            return null;
        }

        if (Input.isKeyJustPressed(GLFW_KEY_ENTER)) {
            String text = currentInput.toString().trim();

            if (!text.isEmpty()) {
                if (inputHistory.isEmpty() || !inputHistory.get(inputHistory.size() - 1).equals(text)) {
                    inputHistory.add(text);
                }
            }

            close();

            if (text.isEmpty()) {
                return null;
            }

            currentInput.setLength(0);
            return text;
        }

        return null;
    }

    public void addMessage(String text, double timeCreated) {
        messages.add(new ChatMessage(text, timeCreated));

        while (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }

    public void render(TextQuadRenderer quadRenderer, FontRenderer fontRenderer, double now) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        float baseX = 12f;
        float baseY = H - 96f;

        float y = baseY;

        Iterator<ChatMessage> it = messages.iterator();
        while (it.hasNext()) {
            ChatMessage msg = it.next();
            double age = now - msg.timeCreated;

            if (!open && age > MESSAGE_LIFETIME) {
                it.remove();
                continue;
            }

            fontRenderer.drawText(quadRenderer, msg.text, baseX, y);
            y -= 18f;
        }

        if (open) {
            float barX = 8f;
            float barY = H -130f;
            float barW = 460f;
            float barH = 24f;

            quadRenderer.drawColoredQuad(barX, barY, barW, barH, 0f, 0f, 0f, 0.65f);

            String prefix = "> ";
            String fullText = prefix + currentInput.toString();
            fontRenderer.drawText(quadRenderer, fullText, barX + 6f, barY + 2f);

            boolean caretVisible = ((int) (caretBlinkTime * 2.0)) % 2 == 0;
            if (caretVisible) {
                String beforeCaret = prefix + currentInput.substring(0, caretPos);
                float caretX = barX + 6f + fontRenderer.getTextWidth(beforeCaret);

                glBindTexture(GL_TEXTURE_2D, 0);
                quadRenderer.drawColoredQuad(caretX, barY + 2f, 2f, 16f, 1f, 1f, 1f, 1f);
            }
        }
    }

    private void deletePreviousWord() {
        if (caretPos <= 0) return;

        while (caretPos > 0 && Character.isWhitespace(currentInput.charAt(caretPos - 1))) {
            currentInput.deleteCharAt(caretPos - 1);
            caretPos--;
        }

        while (caretPos > 0 && !Character.isWhitespace(currentInput.charAt(caretPos - 1))) {
            currentInput.deleteCharAt(caretPos - 1);
            caretPos--;
        }
    }
}