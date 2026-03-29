package items;

import render.Texture;

public class ItemDef {

    public final int id;
    public final String name;
    public final RenderKind renderKind;
    public final Texture texture; // only used for FLAT items

    public ItemDef(int id, String name, RenderKind renderKind, Texture texture) {
        this.id = id;
        this.name = name;
        this.renderKind = renderKind;
        this.texture = texture;
    }
}