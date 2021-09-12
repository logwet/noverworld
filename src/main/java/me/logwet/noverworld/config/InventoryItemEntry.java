package me.logwet.noverworld.config;

public class InventoryItemEntry {
    private final String name;
    private final int slot;

    public InventoryItemEntry(String name, int slot) {
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
