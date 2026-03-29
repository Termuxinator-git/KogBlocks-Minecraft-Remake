package blocks;

import items.ItemTypes;
import world.Chunk;

public class BlockDrops {

    public static int getDrop(int blockId) {
        switch (blockId) {
            case Chunk.GRASS:
                return ItemTypes.DIRT;

            case Chunk.DIRT:
                return ItemTypes.DIRT;

            case Chunk.STONE:
                return ItemTypes.COBBLESTONE;

            case Chunk.OAK_LOG:
                return ItemTypes.OAK_LOG;

            case Chunk.OAK_LEAVES:
                return Math.random() < 0.05 ? ItemTypes.STICK : Chunk.OAK_LEAVES;

            case Chunk.OAK_PLANKS:
                return ItemTypes.OAK_PLANKS;
                
            case Chunk.CRAFTING_TABLE:
                return ItemTypes.CRAFTING_TABLE;

            default:
                return blockId;
        }
    }
}