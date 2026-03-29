package inventory;

import blocks.BlockTypes;

public class Inventory {
    public static final int SIZE = 36;
    public static final int HOTBAR_START = 27;
    private static final int MAX_STACK = 64;

    private final int[] items = new int[SIZE];
    private final int[] counts = new int[SIZE];

    public Inventory() {
        for (int i = 0; i < SIZE; i++) {
            items[i] = BlockTypes.AIR;
            counts[i] = 0;
        }
    }

    public int getItem(int slot) {
        return items[slot];
    }

    public int getCount(int slot) {
        return counts[slot];
    }

    public int getMaxStack() {
        return MAX_STACK;
    }

    public void setSlot(int slot, int item, int count) {
        if (count <= 0 || item == BlockTypes.AIR) {
            items[slot] = BlockTypes.AIR;
            counts[slot] = 0;
        } else {
            items[slot] = item;
            counts[slot] = count;
        }
    }

    public int getHotbarItem(int selectedSlot) {
        return items[HOTBAR_START + selectedSlot];
    }

    public int getHotbarCount(int selectedSlot) {
        return counts[HOTBAR_START + selectedSlot];
    }

    public void removeFromHotbar(int selectedSlot) {
        int slot = HOTBAR_START + selectedSlot;
        if (items[slot] == BlockTypes.AIR) return;

        counts[slot]--;
        if (counts[slot] <= 0) {
            items[slot] = BlockTypes.AIR;
            counts[slot] = 0;
        }
    }

    public void addItem(int item) {
        if (item == BlockTypes.AIR) return;

        for (int i = HOTBAR_START; i < SIZE; i++) {
            if (items[i] == item && counts[i] < MAX_STACK) {
                counts[i]++;
                return;
            }
        }

        for (int i = 0; i < HOTBAR_START; i++) {
            if (items[i] == item && counts[i] < MAX_STACK) {
                counts[i]++;
                return;
            }
        }

        for (int i = HOTBAR_START; i < SIZE; i++) {
            if (items[i] == BlockTypes.AIR) {
                items[i] = item;
                counts[i] = 1;
                return;
            }
        }

        for (int i = 0; i < HOTBAR_START; i++) {
            if (items[i] == BlockTypes.AIR) {
                items[i] = item;
                counts[i] = 1;
                return;
            }
        }
    }
}