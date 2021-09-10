package me.logwet.noverworld.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixedConfig {
    public int[] getSpawnShiftRange() {
        return spawnShiftRange;
    }

    public void setSpawnShiftRange(int[] spawnShiftRange) {
        this.spawnShiftRange = spawnShiftRange;
    }

    private int[] spawnShiftRange;

    private List<FixedConfigInventoryItemEntry> inventory;

    public List<FixedConfigInventoryItemEntry> getInventory() {
        return inventory;
    }

    public Map<String, int[]> getUniqueItems() {
        Map<String, int[]> returnValues = new HashMap<>();
        inventory.forEach(item -> {
            if (item.isUnique()) {
                returnValues.put(item.getName(), new int[]{item.getCount(), item.getDamage(), item.getSlot()});
            }
        });
        return returnValues;
    }

    public List<NonUniqueItem> getNonUniqueItems() {
        List<NonUniqueItem> returnValues = new ArrayList<>();
        inventory.forEach(item -> {
            if (!item.isUnique()) {
                returnValues.add(new NonUniqueItem(item.getName(), new int[]{item.getCount(), item.getDamage(), item.getSlot()}));
            }
        });
        return returnValues;
    }

}
