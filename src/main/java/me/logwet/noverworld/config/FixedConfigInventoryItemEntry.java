package me.logwet.noverworld.config;

public class FixedConfigInventoryItemEntry {
    private String name;
    private int count;
    private int damage;
    private int slot;
    private boolean editable;
    private boolean unique;

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public int getDamage() {
        return damage;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isUnique() {
        return unique;
    }
}
