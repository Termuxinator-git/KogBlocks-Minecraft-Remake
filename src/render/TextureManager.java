package render;


public class TextureManager {

    public static Texture dirt;
    public static Texture grassTop;
    public static Texture grassSide;
    public static Texture stone;
    public static Texture oakLogTop;
    public static Texture oakLogSide;
    public static Texture oakLeaves;
    public static Texture oakPlanks;
    public static Texture craftingTableTop;
    public static Texture craftingTableSide;
    public static Texture craftingTableFront;
    public static Texture cobblestone;
    
    public static Texture stick;
    public static Texture woodenPickaxe;
    public static Texture woodenShovel;
    public static Texture woodenAxe;

    public static void init() {
        dirt      = new Texture("textures/nature/dirt.png");
        grassTop  = new Texture("textures/nature/grass_top.png");
        grassSide = new Texture("textures/nature/grass_side.png");
        stone     = new Texture("textures/nature/stone.png");
        oakLogTop  = new Texture("textures/nature/oak_top_bottom.png");
        oakLogSide = new Texture("textures/nature/oak_side.png");
        oakLeaves  = new Texture("textures/nature/oak_leaves.png");
        oakPlanks  = new Texture("textures/construction/oak_planks.png");
        craftingTableTop   = new Texture("textures/utility/crafting_table_top.png");
        craftingTableSide  = new Texture("textures/utility/crafting_table_side.png");
        craftingTableFront = new Texture("textures/utility/crafting_table_front.png");
        cobblestone = new Texture("textures/nature/cobblestone.png");
        
        stick = new Texture("textures/items/nature/stick.png");
        woodenPickaxe = new Texture("textures/items/tools/wooden_pickaxe.png");
        woodenShovel  = new Texture("textures/items/tools/wooden_shovel.png");
        woodenAxe     = new Texture("textures/items/tools/wooden_axe.png");
        
        
    }
}