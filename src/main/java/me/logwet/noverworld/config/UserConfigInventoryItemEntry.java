package me.logwet.noverworld.config;

public class UserConfigInventoryItemEntry {
    private String name;
    private int slot;

    public UserConfigInventoryItemEntry(String name, int slot) {
        this.name = name;
        this.slot = slot;
    }

    public String getName() {
        return name;
    }

    public int getSlot() {
        return slot;
    }
}
