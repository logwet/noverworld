package me.logwet.noverworld.config;

import me.logwet.noverworld.util.RandomDistribution;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FixedConfig {
    private RandomDistribution spawnShiftRange;

    private RandomDistribution spawnYHeightDistribution;

    private Map<String, Float> playerAttributes;

    private List<InventoryItemEntry> inventory;

    public RandomDistribution getSpawnShiftRange() {
        return spawnShiftRange;
    }

    @NotNull
    public RandomDistribution getSpawnYHeightDistribution() {
        return spawnYHeightDistribution;
    }

    @NotNull
    public Map<String, Float> getPlayerAttributes() {
        return playerAttributes;
    }

    @NotNull
    public List<InventoryItemEntry> getInventory() {
        return inventory;
    }

    @NotNull
    public List<InventoryItemEntry> getUniqueItems() {
        return inventory
                .stream()
                .filter(item -> item.isUnique() && item.isEditable())
                .collect(Collectors.toList());
    }

    @NotNull
    public List<InventoryItemEntry> getNonUniqueItems() {
        return inventory
                .stream()
                .filter(item -> !(item.isUnique() && item.isEditable()))
                .collect(Collectors.toList());
    }
}
