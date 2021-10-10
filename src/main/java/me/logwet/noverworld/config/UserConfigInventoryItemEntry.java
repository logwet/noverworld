package me.logwet.noverworld.config;

import org.jetbrains.annotations.NotNull;

public class UserConfigInventoryItemEntry {
    @NotNull
    protected String name;

    @NotNull
    protected Integer slot;

    public UserConfigInventoryItemEntry(@NotNull String name, @NotNull Integer slot) {
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
