package me.logwet.noverworld.config;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.stream.IntStream;

public class InventoryItemEntry {
    private String name;
    private String count;
    private int damage;
    private int slot;
    private boolean editable;
    private boolean unique;

    public String getName() {
        return name;
    }

    public String getRawCount() {
        return count;
    }

    public int getCount(@NotNull Random random) {
        String[] stringRange = getRawCount().split("-");
        int[] range = IntStream.range(Integer.parseInt(stringRange[0]), Integer.parseInt(stringRange[1])+1).toArray();
        if (range.length < 1) {
            return 0;
        }
        return range[random.nextInt(range.length)];
    }

    public int getDamage() {
        return damage;
    }

    public int getSlot() {
        return slot;
    }

    public int getPrettySlot() {
        return slot + 1;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isUnique() {
        return unique;
    }
}
