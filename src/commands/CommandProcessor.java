package commands;

import entity.ItemEntity;
import entity.Player;
import inventory.Inventory;
import items.ItemTypes;
import org.joml.Vector3f;
import world.World;

public class CommandProcessor {

    public static String execute(String input, Inventory inventory, World world, Player player) {
        if (input == null || input.isBlank()) {
            return null;
        }

        if (!input.startsWith("/")) {
            return input;
        }

        String[] parts = input.substring(1).trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return "Unknown command.";
        }

        String command = parts[0].toLowerCase();

        switch (command) {
            case "give":
                return handleGive(parts, inventory, world, player);

            default:
                return "Unknown command: " + command;
        }
    }

    private static String handleGive(String[] parts, Inventory inventory, World world, Player player) {
        if (parts.length < 3) {
            return "Usage: /give @s <item> [count]";
        }

        String target = parts[1];
        if (!target.equalsIgnoreCase("@s")) {
            return "Only @s is supported right now.";
        }

        String itemName = parts[2].toLowerCase();
        int itemId = getItemIdByName(itemName);

        if (itemId == -1) {
            return "Unknown item: " + itemName;
        }

        int count = 1;
        if (parts.length >= 4) {
            try {
                count = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                return "Invalid count: " + parts[3];
            }
        }

        if (count <= 0) {
            return "Count must be at least 1.";
        }

        int remaining = count;

        while (remaining > 0) {
            if (hasInventorySpaceFor(inventory, itemId)) {
                inventory.addItem(itemId);
            } else {
                dropItemNearPlayer(world, player, itemId);
            }
            remaining--;
        }

        return "Given " + count + " " + itemName + ".";
    }

    private static int getItemIdByName(String name) {
        switch (name) {
            case "stick":
                return ItemTypes.STICK;
            case "dirt":
            case "dirt_block":
                return ItemTypes.DIRT;
            case "grass_block":
            case "grass":
                return ItemTypes.GRASS;
            case "stone_block":
            case "stone":
                return ItemTypes.STONE;
            case "cobblestone":
                return ItemTypes.COBBLESTONE;
            case "oak_log":
                return ItemTypes.OAK_LOG;
            case "oak_leaves":
                return ItemTypes.OAK_LEAVES;
            case "oak_planks":
            case "planks":
                return ItemTypes.OAK_PLANKS;
            case "crafting_table":
                return ItemTypes.CRAFTING_TABLE;
            
            case "wooden_pickaxe":
                return ItemTypes.WOODEN_PICKAXE;

            case "wooden_shovel":
                return ItemTypes.WOODEN_SHOVEL;

            case "wooden_axe":
                return ItemTypes.WOODEN_AXE;
            
            
            default:
                return -1;
        }
    }

    private static boolean hasInventorySpaceFor(Inventory inventory, int itemId) {
        for (int i = 0; i < Inventory.SIZE; i++) {
            if (inventory.getItem(i) == itemId && inventory.getCount(i) < inventory.getMaxStack()) {
                return true;
            }
        }

        for (int i = 0; i < Inventory.SIZE; i++) {
            if (inventory.getItem(i) == 0) {
                return true;
            }
        }

        return false;
    }

    private static void dropItemNearPlayer(World world, Player player, int itemId) {
        Vector3f pos = player.position;

        float x = pos.x;
        float y = pos.y + 1.0f;
        float z = pos.z;

        world.spawnItem(itemId, 1, x, y, z);

        if (!world.getItemEntities().isEmpty()) {
            ItemEntity dropped = world.getItemEntities().get(world.getItemEntities().size() - 1);
            dropped.pickupDelay = 0.75f;
        }
    }
}