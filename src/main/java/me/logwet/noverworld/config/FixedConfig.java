package me.logwet.noverworld.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FixedConfig {
    private int[] spawnShiftRange;

    private Map<String, Integer> spawnYHeightDistribution;

    private Map<String, Float> playerAttributes;

    private List<FixedConfigInventoryItemEntry> inventory;

    public int[] getSpawnShiftRange() {
        return spawnShiftRange;
    }

    public Map<String, Integer> getSpawnYHeightDistribution() {
        return spawnYHeightDistribution;
    }

    public Map<String, Float> getPlayerAttributes() {
        return playerAttributes;
    }

    public List<FixedConfigInventoryItemEntry> getInventory() {
        return inventory;
    }

    public List<FixedConfigInventoryItemEntry> getUniqueItems() {
        return inventory
                .stream()
                .filter(item -> item.isUnique() && item.isEditable())
                .collect(Collectors.toList());
    }

    public List<FixedConfigInventoryItemEntry> getNonUniqueItems() {
        return inventory
                .stream()
                .filter(item -> !(item.isUnique() && item.isEditable()))
                .collect(Collectors.toList());
    }
}
