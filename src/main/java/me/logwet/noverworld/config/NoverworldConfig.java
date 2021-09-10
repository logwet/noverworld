package me.logwet.noverworld.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoverworldConfig {
    private boolean test;

    private List<InventoryItemEntry> inventory;

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

    public NoverworldConfig() {
        inventory = new ArrayList<>();
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}

