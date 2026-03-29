package render;

public interface TextQuadRenderer {
    void drawTextQuad(float x, float y, float w, float h);
    void drawColoredQuad(float x, float y, float w, float h, float r, float g, float b, float a);
}