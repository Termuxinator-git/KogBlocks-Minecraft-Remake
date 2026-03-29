package items;

import java.util.HashMap;
import java.util.Map;

import render.TextureManager;

public class ItemRegistry {

    private static final Map<Integer, ItemDef> items = new HashMap<>();

    public static void init() {

    	register(new ItemDef(ItemTypes.DIRT, "Dirt", RenderKind.BLOCK, null));
    	register(new ItemDef(ItemTypes.GRASS, "Grass", RenderKind.BLOCK, null));
    	register(new ItemDef(ItemTypes.STONE, "Stone", RenderKind.BLOCK, null));
    	register(new ItemDef(ItemTypes.OAK_LOG, "Oak Log", RenderKind.BLOCK, null));
    	register(new ItemDef(ItemTypes.OAK_LEAVES, "Oak Leaves", RenderKind.BLOCK, null));
    	register(new ItemDef(ItemTypes.OAK_PLANKS, "Oak Planks", RenderKind.BLOCK, null));
    	register(new ItemDef(ItemTypes.STICK, "Stick", RenderKind.FLAT, TextureManager.stick));
    	register(new ItemDef(ItemTypes.CRAFTING_TABLE, "Crafting Table", RenderKind.BLOCK, null));
    	register(new ItemDef(ItemTypes.COBBLESTONE, "Cobblestone", RenderKind.BLOCK, null));
    	
        register(new ItemDef(ItemTypes.WOODEN_PICKAXE, "Wooden Pickaxe", RenderKind.FLAT, TextureManager.woodenPickaxe));
        register(new ItemDef(ItemTypes.WOODEN_SHOVEL, "Wooden Shovel", RenderKind.FLAT, TextureManager.woodenShovel));
        register(new ItemDef(ItemTypes.WOODEN_AXE, "Wooden Axe", RenderKind.FLAT, TextureManager.woodenAxe));
    	
    }

    private static void register(ItemDef item) {
        items.put(item.id, item);
    }

    public static ItemDef get(int id) {
        return items.get(id);
    }
}