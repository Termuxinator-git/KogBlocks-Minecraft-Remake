package gui;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import core.Input;
import inventory.Hotbar;
import inventory.Inventory;
import items.ItemTypes;
import render.Texture;
import world.Chunk;

public class CraftingTableScreen {
    private Texture craftingTableTex;

    private static final int MAX_STACK = 64;

    private static final float W = 1024f;
    private static final float H = 768f;

    private static final float SCALE = 2f;
    private static final float WIDTH = 176f * SCALE;
    private static final float HEIGHT = 166f * SCALE;

    private static final float SLOT_STEP = 18f * SCALE;
    private static final float SLOT_HIT = 16f * SCALE;

    private static final double DOUBLE_CLICK_WINDOW = 0.25;

    private static final float PLAYER_INV_OFFSET_Y = -10.1f;
    private static final float HOTBAR_OFFSET_Y = -10.1f;

    private static final float CRAFT_GRID_OFFSET_X = 0f;
    private static final float CRAFT_GRID_OFFSET_Y = 0f;
    private static final float CRAFT_RESULT_OFFSET_X = 0f;
    private static final float CRAFT_RESULT_OFFSET_Y = -10f;

    private int heldItem = Chunk.AIR;
    private int heldCount = 0;

    private final int[][] craftGrid = new int[3][3];
    private final int[][] craftCounts = new int[3][3];
    private int craftResult = Chunk.AIR;
    private int craftResultCount = 0;

    private boolean rightDragging = false;
    private final boolean[] dragVisited = new boolean[Inventory.SIZE + 9];

    private double lastLeftClickTime = 0.0;
    private int lastLeftClickSlot = -1;

    public void init() {
        craftingTableTex = new Texture("textures/gui/crafting_table.png");
    }

    public void render(Hotbar hotbar) {
        float drawX = (W - WIDTH) / 2f;
        float drawY = (H - HEIGHT) / 2f;

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        craftingTableTex.bind();
        hotbar.drawTextQuadFlipped(drawX, drawY, WIDTH, HEIGHT);

        Inventory inv = hotbar.getInventory();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int item = craftGrid[row][col];
                if (item == Chunk.AIR) continue;

                float slotX = drawX + 30f * SCALE + CRAFT_GRID_OFFSET_X + col * SLOT_STEP;
                float slotY = drawY + 12f * SCALE + CRAFT_GRID_OFFSET_Y + row * SLOT_STEP;

                hotbar.renderInventoryItem(item, craftCounts[row][col], slotX, slotY);
            }
        }

        if (craftResult != Chunk.AIR) {
            float resultX = drawX + 124f * SCALE + CRAFT_RESULT_OFFSET_X;
            float resultY = drawY + 35f * SCALE + CRAFT_RESULT_OFFSET_Y;
            hotbar.renderInventoryItem(craftResult, craftResultCount, resultX, resultY);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = row * 9 + col;

                float slotX = drawX + 8f * SCALE + col * SLOT_STEP;
                float slotY = drawY + 84f * SCALE + PLAYER_INV_OFFSET_Y + row * SLOT_STEP;

                renderInventorySlot(hotbar, inv, slot, slotX, slotY);
            }
        }

        for (int col = 0; col < 9; col++) {
            int slot = Inventory.HOTBAR_START + col;

            float slotX = drawX + 8f * SCALE + col * SLOT_STEP;
            float slotY = drawY + 142f * SCALE + HOTBAR_OFFSET_Y;

            renderInventorySlot(hotbar, inv, slot, slotX, slotY);
        }

        if (heldItem != Chunk.AIR) {
            float heldX = (float) Input.getMouseX() - 24f;
            float heldY = (float) Input.getMouseY() - 24f;
            hotbar.renderInventoryItem(heldItem, heldCount, heldX, heldY);
        }

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderInventorySlot(Hotbar hotbar, Inventory inv, int slot, float slotX, float slotY) {
        int item = inv.getItem(slot);
        int count = inv.getCount(slot);

        if (item == Chunk.AIR) return;
        hotbar.renderInventoryItem(item, count, slotX, slotY);
    }

    public void handleInput(Hotbar hotbar) {
        float mx = (float) Input.getMouseX();
        float my = (float) Input.getMouseY();

        Inventory inv = hotbar.getInventory();
        int hoveredSlot = getSlotAt(mx, my);
        int craftSlot = getCraftSlotAt(mx, my);
        boolean overResult = isOverCraftResult(mx, my);

        if (Input.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            if (overResult && craftResult != Chunk.AIR) {
                for (int i = 0; i < craftResultCount; i++) {
                    hotbar.addItem(craftResult);
                }

                consumeCurrentCraftRecipe();
                updateCrafting();
                return;
            }

            if (craftSlot != -1) {
                int row = craftSlot / 3;
                int col = craftSlot % 3;

                double now = glfwGetTime();
                int craftDoubleClickSlot = Inventory.SIZE + craftSlot;
                boolean doubleClick =
                    craftDoubleClickSlot == lastLeftClickSlot &&
                    (now - lastLeftClickTime) <= DOUBLE_CLICK_WINDOW;

                if (doubleClick) {
                    int targetItem = heldItem != Chunk.AIR ? heldItem : craftGrid[row][col];
                    if (targetItem != Chunk.AIR) {
                        if (heldItem == Chunk.AIR) {
                            heldItem = targetItem;
                            heldCount = 0;
                        }
                        collectMatchingFromCraftGrid(targetItem);
                        collectMatching(inv, hoveredSlot == -1 ? 0 : hoveredSlot);
                    }

                    lastLeftClickTime = 0.0;
                    lastLeftClickSlot = -1;
                } else {
                    handleCraftLeftClick(row, col);
                    lastLeftClickTime = now;
                    lastLeftClickSlot = craftDoubleClickSlot;
                }
                return;
            }

            if (hoveredSlot != -1) {
                double now = glfwGetTime();
                boolean doubleClick =
                    hoveredSlot == lastLeftClickSlot &&
                    (now - lastLeftClickTime) <= DOUBLE_CLICK_WINDOW;

                if (doubleClick) {
                    int targetItem = heldItem != Chunk.AIR ? heldItem : inv.getItem(hoveredSlot);
                    if (targetItem != Chunk.AIR) {
                        if (heldItem == Chunk.AIR) {
                            heldItem = targetItem;
                            heldCount = 0;
                        }
                        collectMatching(inv, hoveredSlot);
                        collectMatchingFromCraftGrid(targetItem);
                    }

                    lastLeftClickTime = 0.0;
                    lastLeftClickSlot = -1;
                } else {
                    handleLeftClick(inv, hoveredSlot);
                    lastLeftClickTime = now;
                    lastLeftClickSlot = hoveredSlot;
                }
            }
        }

        if (Input.isMouseJustPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (heldItem != Chunk.AIR) {
                rightDragging = true;
                resetDragVisited();
            }

            if (craftSlot != -1) {
                int row = craftSlot / 3;
                int col = craftSlot % 3;
                handleCraftRightClick(row, col);

                if (rightDragging) {
                    dragVisited[Inventory.SIZE + row * 3 + col] = true;
                }
                return;
            }

            if (hoveredSlot != -1) {
                handleRightClick(inv, hoveredSlot);

                if (rightDragging) {
                    dragVisited[hoveredSlot] = true;
                }
            }
        }

        if (rightDragging && Input.isMouseDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (craftSlot != -1) {
                int row = craftSlot / 3;
                int col = craftSlot % 3;
                handleCraftRightDrag(row, col);
            } else if (hoveredSlot != -1) {
                handleRightDrag(inv, hoveredSlot);
            }
        }

        if (!Input.isMouseDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            rightDragging = false;
        }
    }

    private void handleLeftClick(Inventory inv, int clickedSlot) {
        if (clickedSlot < 0) return;

        int slotItem = inv.getItem(clickedSlot);
        int slotCount = inv.getCount(clickedSlot);

        if (heldItem == Chunk.AIR) {
            if (slotItem != Chunk.AIR) {
                heldItem = slotItem;
                heldCount = slotCount;
                inv.setSlot(clickedSlot, Chunk.AIR, 0);
            }
            return;
        }

        if (slotItem == Chunk.AIR) {
            inv.setSlot(clickedSlot, heldItem, heldCount);
            heldItem = Chunk.AIR;
            heldCount = 0;
            return;
        }

        if (slotItem == heldItem && slotCount < inv.getMaxStack()) {
            int move = Math.min(inv.getMaxStack() - slotCount, heldCount);
            inv.setSlot(clickedSlot, slotItem, slotCount + move);
            heldCount -= move;

            if (heldCount <= 0) {
                heldItem = Chunk.AIR;
                heldCount = 0;
            }
            return;
        }

        inv.setSlot(clickedSlot, heldItem, heldCount);
        heldItem = slotItem;
        heldCount = slotCount;
    }

    private void handleRightClick(Inventory inv, int clickedSlot) {
        int slotItem = inv.getItem(clickedSlot);
        int slotCount = inv.getCount(clickedSlot);

        if (heldItem == Chunk.AIR) {
            if (slotItem != Chunk.AIR) {
                int take = (slotCount + 1) / 2;
                int remain = slotCount - take;

                heldItem = slotItem;
                heldCount = take;

                if (remain > 0) {
                    inv.setSlot(clickedSlot, slotItem, remain);
                } else {
                    inv.setSlot(clickedSlot, Chunk.AIR, 0);
                }
            }
            return;
        }

        if (slotItem == Chunk.AIR) {
            inv.setSlot(clickedSlot, heldItem, 1);
            heldCount--;
        } else if (slotItem == heldItem && slotCount < inv.getMaxStack()) {
            inv.setSlot(clickedSlot, slotItem, slotCount + 1);
            heldCount--;
        }

        if (heldCount <= 0) {
            heldItem = Chunk.AIR;
            heldCount = 0;
        }
    }

    private void handleRightDrag(Inventory inv, int clickedSlot) {
        if (heldItem == Chunk.AIR || heldCount <= 0) return;
        if (dragVisited[clickedSlot]) return;

        int slotItem = inv.getItem(clickedSlot);
        int slotCount = inv.getCount(clickedSlot);

        if (slotItem == Chunk.AIR) {
            inv.setSlot(clickedSlot, heldItem, 1);
            heldCount--;
            dragVisited[clickedSlot] = true;
        } else if (slotItem == heldItem && slotCount < inv.getMaxStack()) {
            inv.setSlot(clickedSlot, slotItem, slotCount + 1);
            heldCount--;
            dragVisited[clickedSlot] = true;
        }

        if (heldCount <= 0) {
            heldItem = Chunk.AIR;
            heldCount = 0;
            rightDragging = false;
        }
    }

    private void handleCraftLeftClick(int row, int col) {
        int slotItem = craftGrid[row][col];
        int slotCount = craftCounts[row][col];

        if (heldItem == Chunk.AIR) {
            if (slotItem != Chunk.AIR) {
                heldItem = slotItem;
                heldCount = slotCount;
                craftGrid[row][col] = Chunk.AIR;
                craftCounts[row][col] = 0;
            }
            updateCrafting();
            return;
        }

        if (slotItem == Chunk.AIR) {
            craftGrid[row][col] = heldItem;
            craftCounts[row][col] = heldCount;
            heldItem = Chunk.AIR;
            heldCount = 0;
            updateCrafting();
            return;
        }

        if (slotItem == heldItem) {
            int space = MAX_STACK - slotCount;
            int move = Math.min(space, heldCount);

            craftCounts[row][col] += move;
            heldCount -= move;

            if (heldCount <= 0) {
                heldItem = Chunk.AIR;
                heldCount = 0;
            }

            updateCrafting();
            return;
        }

        craftGrid[row][col] = heldItem;
        craftCounts[row][col] = heldCount;
        heldItem = slotItem;
        heldCount = slotCount;

        updateCrafting();
    }

    private void handleCraftRightClick(int row, int col) {
        int slotItem = craftGrid[row][col];
        int slotCount = craftCounts[row][col];

        if (heldItem == Chunk.AIR) {
            if (slotItem != Chunk.AIR) {
                int take = (slotCount + 1) / 2;
                int remain = slotCount - take;

                heldItem = slotItem;
                heldCount = take;

                if (remain > 0) {
                    craftGrid[row][col] = slotItem;
                    craftCounts[row][col] = remain;
                } else {
                    craftGrid[row][col] = Chunk.AIR;
                    craftCounts[row][col] = 0;
                }
            }

            updateCrafting();
            return;
        }

        if (slotItem == Chunk.AIR) {
            craftGrid[row][col] = heldItem;
            craftCounts[row][col] = 1;
            heldCount--;
        } else if (slotItem == heldItem && slotCount < MAX_STACK) {
            craftCounts[row][col] = slotCount + 1;
            heldCount--;
        }

        if (heldCount <= 0) {
            heldItem = Chunk.AIR;
            heldCount = 0;
        }

        updateCrafting();
    }

    private void handleCraftRightDrag(int row, int col) {
        int dragIndex = Inventory.SIZE + row * 3 + col;
        if (heldItem == Chunk.AIR || heldCount <= 0) return;
        if (dragVisited[dragIndex]) return;

        int slotItem = craftGrid[row][col];
        int slotCount = craftCounts[row][col];

        if (slotItem == Chunk.AIR) {
            craftGrid[row][col] = heldItem;
            craftCounts[row][col] = 1;
            heldCount--;
            dragVisited[dragIndex] = true;
        } else if (slotItem == heldItem && slotCount < MAX_STACK) {
            craftCounts[row][col] = slotCount + 1;
            heldCount--;
            dragVisited[dragIndex] = true;
        }

        if (heldCount <= 0) {
            heldItem = Chunk.AIR;
            heldCount = 0;
            rightDragging = false;
        }

        updateCrafting();
    }

    private void collectMatching(Inventory inv, int clickedSlot) {
        int targetItem = heldItem != Chunk.AIR
            ? heldItem
            : (clickedSlot >= 0 ? inv.getItem(clickedSlot) : Chunk.AIR);

        if (targetItem == Chunk.AIR) return;

        if (heldItem == Chunk.AIR) {
            if (clickedSlot < 0) return;

            int slotCount = inv.getCount(clickedSlot);
            heldItem = targetItem;
            heldCount = slotCount;
            inv.setSlot(clickedSlot, Chunk.AIR, 0);
        }

        if (heldItem != targetItem) return;

        int max = inv.getMaxStack();

        for (int i = 0; i < Inventory.SIZE; i++) {
            if (heldCount >= max) break;
            if (inv.getItem(i) != targetItem) continue;

            int slotCount = inv.getCount(i);
            int move = Math.min(max - heldCount, slotCount);

            heldCount += move;

            int remain = slotCount - move;
            if (remain > 0) {
                inv.setSlot(i, targetItem, remain);
            } else {
                inv.setSlot(i, Chunk.AIR, 0);
            }
        }
    }

    private void collectMatchingFromCraftGrid(int targetItem) {
        if (targetItem == Chunk.AIR) return;
        if (heldItem != targetItem) return;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (heldCount >= MAX_STACK) return;
                if (craftGrid[row][col] != targetItem) continue;

                int slotCount = craftCounts[row][col];
                int move = Math.min(MAX_STACK - heldCount, slotCount);

                heldCount += move;

                int remain = slotCount - move;
                if (remain > 0) {
                    craftCounts[row][col] = remain;
                } else {
                    craftGrid[row][col] = Chunk.AIR;
                    craftCounts[row][col] = 0;
                }
            }
        }

        updateCrafting();
    }

    private int countNonAirCraftSlots() {
        int count = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (craftGrid[row][col] != Chunk.AIR && craftCounts[row][col] > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private void updateCrafting() {
        craftResult = Chunk.AIR;
        craftResultCount = 0;

        if (countNonAirCraftSlots() == 5 &&
            craftGrid[0][0] == Chunk.OAK_PLANKS &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[0][2] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == ItemTypes.STICK &&
            craftGrid[2][1] == ItemTypes.STICK &&
            craftCounts[0][0] > 0 && craftCounts[0][1] > 0 && craftCounts[0][2] > 0 &&
            craftCounts[1][1] > 0 && craftCounts[2][1] > 0) {
            craftResult = ItemTypes.WOODEN_PICKAXE;
            craftResultCount = 1;
            return;
        }

        if (countNonAirCraftSlots() == 3 &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == ItemTypes.STICK &&
            craftGrid[2][1] == ItemTypes.STICK &&
            craftCounts[0][1] > 0 &&
            craftCounts[1][1] > 0 &&
            craftCounts[2][1] > 0) {
            craftResult = ItemTypes.WOODEN_SHOVEL;
            craftResultCount = 1;
            return;
        }

        if (countNonAirCraftSlots() == 5 &&
            craftGrid[0][0] == Chunk.OAK_PLANKS &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[1][0] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == ItemTypes.STICK &&
            craftGrid[2][1] == ItemTypes.STICK &&
            craftCounts[0][0] > 0 && craftCounts[0][1] > 0 &&
            craftCounts[1][0] > 0 && craftCounts[1][1] > 0 &&
            craftCounts[2][1] > 0) {
            craftResult = ItemTypes.WOODEN_AXE;
            craftResultCount = 1;
            return;
        }

        if (countNonAirCraftSlots() == 5 &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[0][2] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == Chunk.OAK_PLANKS &&
            craftGrid[1][0] == ItemTypes.STICK &&
            craftGrid[2][0] == ItemTypes.STICK &&
            craftCounts[0][1] > 0 && craftCounts[0][2] > 0 &&
            craftCounts[1][1] > 0 && craftCounts[1][0] > 0 &&
            craftCounts[2][0] > 0) {
            craftResult = ItemTypes.WOODEN_AXE;
            craftResultCount = 1;
            return;
        }

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                if (countNonAirCraftSlots() == 4 &&
                    craftGrid[row][col] == Chunk.OAK_PLANKS &&
                    craftGrid[row][col + 1] == Chunk.OAK_PLANKS &&
                    craftGrid[row + 1][col] == Chunk.OAK_PLANKS &&
                    craftGrid[row + 1][col + 1] == Chunk.OAK_PLANKS &&
                    craftCounts[row][col] > 0 &&
                    craftCounts[row][col + 1] > 0 &&
                    craftCounts[row + 1][col] > 0 &&
                    craftCounts[row + 1][col + 1] > 0) {
                    craftResult = Chunk.CRAFTING_TABLE;
                    craftResultCount = 1;
                    return;
                }
            }
        }

        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 2; row++) {
                if (countNonAirCraftSlots() == 2 &&
                    craftGrid[row][col] == Chunk.OAK_PLANKS &&
                    craftGrid[row + 1][col] == Chunk.OAK_PLANKS &&
                    craftCounts[row][col] > 0 &&
                    craftCounts[row + 1][col] > 0) {
                    craftResult = ItemTypes.STICK;
                    craftResultCount = 4;
                    return;
                }
            }
        }

        int totalLogs = 0;
        int nonAirSlots = countNonAirCraftSlots();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (craftGrid[row][col] == Chunk.OAK_LOG && craftCounts[row][col] > 0) {
                    totalLogs += craftCounts[row][col];
                }
            }
        }

        if (totalLogs > 0 && allCraftItemsAre(Chunk.OAK_LOG)) {
            craftResult = Chunk.OAK_PLANKS;
            craftResultCount = 4;
        }
    }
    
    private boolean allCraftItemsAre(int itemId) {
        boolean foundAny = false;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (craftCounts[row][col] <= 0 || craftGrid[row][col] == Chunk.AIR) {
                    continue;
                }

                foundAny = true;

                if (craftGrid[row][col] != itemId) {
                    return false;
                }
            }
        }

        return foundAny;
    }

    private void consumeCurrentCraftRecipe() {
        if (countNonAirCraftSlots() == 5 &&
            craftGrid[0][0] == Chunk.OAK_PLANKS &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[0][2] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == ItemTypes.STICK &&
            craftGrid[2][1] == ItemTypes.STICK &&
            craftCounts[0][0] > 0 && craftCounts[0][1] > 0 && craftCounts[0][2] > 0 &&
            craftCounts[1][1] > 0 && craftCounts[2][1] > 0) {
            consumeCraftSlot(0, 0, 1);
            consumeCraftSlot(0, 1, 1);
            consumeCraftSlot(0, 2, 1);
            consumeCraftSlot(1, 1, 1);
            consumeCraftSlot(2, 1, 1);
            return;
        }

        if (countNonAirCraftSlots() == 3 &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == ItemTypes.STICK &&
            craftGrid[2][1] == ItemTypes.STICK &&
            craftCounts[0][1] > 0 &&
            craftCounts[1][1] > 0 &&
            craftCounts[2][1] > 0) {
            consumeCraftSlot(0, 1, 1);
            consumeCraftSlot(1, 1, 1);
            consumeCraftSlot(2, 1, 1);
            return;
        }

        if (countNonAirCraftSlots() == 5 &&
            craftGrid[0][0] == Chunk.OAK_PLANKS &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[1][0] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == ItemTypes.STICK &&
            craftGrid[2][1] == ItemTypes.STICK &&
            craftCounts[0][0] > 0 && craftCounts[0][1] > 0 &&
            craftCounts[1][0] > 0 && craftCounts[1][1] > 0 &&
            craftCounts[2][1] > 0) {
            consumeCraftSlot(0, 0, 1);
            consumeCraftSlot(0, 1, 1);
            consumeCraftSlot(1, 0, 1);
            consumeCraftSlot(1, 1, 1);
            consumeCraftSlot(2, 1, 1);
            return;
        }

        if (countNonAirCraftSlots() == 5 &&
            craftGrid[0][1] == Chunk.OAK_PLANKS &&
            craftGrid[0][2] == Chunk.OAK_PLANKS &&
            craftGrid[1][1] == Chunk.OAK_PLANKS &&
            craftGrid[1][0] == ItemTypes.STICK &&
            craftGrid[2][0] == ItemTypes.STICK &&
            craftCounts[0][1] > 0 && craftCounts[0][2] > 0 &&
            craftCounts[1][1] > 0 && craftCounts[1][0] > 0 &&
            craftCounts[2][0] > 0) {
            consumeCraftSlot(0, 1, 1);
            consumeCraftSlot(0, 2, 1);
            consumeCraftSlot(1, 1, 1);
            consumeCraftSlot(1, 0, 1);
            consumeCraftSlot(2, 0, 1);
            return;
        }

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                if (countNonAirCraftSlots() == 4 &&
                    craftGrid[row][col] == Chunk.OAK_PLANKS &&
                    craftGrid[row][col + 1] == Chunk.OAK_PLANKS &&
                    craftGrid[row + 1][col] == Chunk.OAK_PLANKS &&
                    craftGrid[row + 1][col + 1] == Chunk.OAK_PLANKS &&
                    craftCounts[row][col] > 0 &&
                    craftCounts[row][col + 1] > 0 &&
                    craftCounts[row + 1][col] > 0 &&
                    craftCounts[row + 1][col + 1] > 0) {
                    consumeCraftSlot(row, col, 1);
                    consumeCraftSlot(row, col + 1, 1);
                    consumeCraftSlot(row + 1, col, 1);
                    consumeCraftSlot(row + 1, col + 1, 1);
                    return;
                }
            }
        }

        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 2; row++) {
                if (countNonAirCraftSlots() == 2 &&
                    craftGrid[row][col] == Chunk.OAK_PLANKS &&
                    craftGrid[row + 1][col] == Chunk.OAK_PLANKS &&
                    craftCounts[row][col] > 0 &&
                    craftCounts[row + 1][col] > 0) {
                    consumeCraftSlot(row, col, 1);
                    consumeCraftSlot(row + 1, col, 1);
                    return;
                }
            }
        }

        outer:
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (craftGrid[row][col] == Chunk.OAK_LOG && craftCounts[row][col] > 0) {
                    craftCounts[row][col]--;
                    if (craftCounts[row][col] <= 0) {
                        craftGrid[row][col] = Chunk.AIR;
                        craftCounts[row][col] = 0;
                    }
                    break outer;
                }
            }
        }
    }

    private void consumeCraftSlot(int row, int col, int amount) {
        craftCounts[row][col] -= amount;
        if (craftCounts[row][col] <= 0) {
            craftCounts[row][col] = 0;
            craftGrid[row][col] = Chunk.AIR;
        }
    }

    private void resetDragVisited() {
        for (int i = 0; i < dragVisited.length; i++) {
            dragVisited[i] = false;
        }
    }

    private int getCraftSlotAt(float mx, float my) {
        float drawX = (W - WIDTH) / 2f;
        float drawY = (H - HEIGHT) / 2f;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                float x = drawX + 30f * SCALE + CRAFT_GRID_OFFSET_X + col * SLOT_STEP;
                float y = drawY + 17f * SCALE + CRAFT_GRID_OFFSET_Y + row * SLOT_STEP;

                if (isInside(mx, my, x, y, SLOT_HIT, SLOT_HIT)) {
                    return row * 3 + col;
                }
            }
        }

        return -1;
    }

    private boolean isOverCraftResult(float mx, float my) {
        float drawX = (W - WIDTH) / 2f;
        float drawY = (H - HEIGHT) / 2f;

        float x = drawX + 124f * SCALE + CRAFT_RESULT_OFFSET_X;
        float y = drawY + 35f * SCALE + CRAFT_RESULT_OFFSET_Y;

        return isInside(mx, my, x, y, SLOT_HIT, SLOT_HIT);
    }

    private int getSlotAt(float mx, float my) {
        float drawX = (W - WIDTH) / 2f;
        float drawY = (H - HEIGHT) / 2f;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                float x = drawX + 8f * SCALE + col * SLOT_STEP;
                float y = drawY + 84f * SCALE + PLAYER_INV_OFFSET_Y + row * SLOT_STEP;

                if (isInside(mx, my, x, y, SLOT_HIT, SLOT_HIT)) {
                    return row * 9 + col;
                }
            }
        }

        for (int col = 0; col < 9; col++) {
            float x = drawX + 8f * SCALE + col * SLOT_STEP;
            float y = drawY + 142f * SCALE + HOTBAR_OFFSET_Y;

            if (isInside(mx, my, x, y, SLOT_HIT, SLOT_HIT)) {
                return Inventory.HOTBAR_START + col;
            }
        }

        return -1;
    }

    private boolean isInside(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}