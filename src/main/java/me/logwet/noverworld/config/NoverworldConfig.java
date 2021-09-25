package me.logwet.noverworld.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoverworldConfig {
    private boolean f3Enabled = true;

    private boolean recipeBookEnabled = true;

    private List<InventoryItemEntry> inventory;

    public NoverworldConfig() {
        inventory = new ArrayList<>();
    }

    public boolean isF3Enabled() {
        return f3Enabled;
    }

    public boolean isRecipeBookEnabled() {
        return recipeBookEnabled;
    }

    public List<InventoryItemEntry> getInventory() {
        return inventory;
    }

    public void setInventory(List<InventoryItemEntry> inventory) {
        this.inventory = inventory;
    }

    public Map<Integer, String> getItems() {
        Map<Integer, String> returnValues = new HashMap<>();
        inventory.forEach(item -> returnValues.put(item.getSlot(), item.getName()));
        return returnValues;
    }
}

