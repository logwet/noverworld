package me.logwet.noverworld.config;

import org.jetbrains.annotations.NotNull;

public class UserConfigInventoryItemEntry {
    protected String name;
    protected int slot;

    public UserConfigInventoryItemEntry(String name, int slot) {
        this.name = name;
        this.slot = slot;
    }

    @NotNull
    public String getName() {
        return name.toLowerCase();
    }

    @NotNull
    public Integer getSlot() {
        return slot;
    }
}
