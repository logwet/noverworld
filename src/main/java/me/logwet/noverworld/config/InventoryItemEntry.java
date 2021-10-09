package me.logwet.noverworld.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.stream.IntStream;

public class InventoryItemEntry extends UserConfigInventoryItemEntry {
    @Nullable
    private String tags;

    @NotNull
    private String count;

    @Nullable
    private Integer damage;

    private boolean editable;

    private boolean unique;

    public InventoryItemEntry(String name, @NotNull String count, int slot, int damage, boolean editable, boolean unique) {
        super(name, slot);
        this.count = count;
        this.damage = damage;
        this.editable = editable;
        this.unique = unique;
    }

    @Nullable
    public String getTags() {
        return tags;
    }

    @NotNull
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

    @Nullable
    public Integer getDamage() {
        return damage;
    }

    @NotNull
    public Integer getPrettySlot() {
        return slot + 1;
    }

    @NotNull
    public Boolean isEditable() {
        return editable;
    }

    @NotNull
    public Boolean isUnique() {
        return unique;
    }
}
