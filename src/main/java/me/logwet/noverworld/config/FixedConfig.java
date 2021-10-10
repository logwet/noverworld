package me.logwet.noverworld.config;

import me.logwet.noverworld.util.RandomDistribution;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FixedConfig {
    @NotNull
    private RandomDistribution spawnShiftRange;

    @NotNull
    private RandomDistribution spawnYHeightDistribution;

    @NotNull
    private Map<String, Float> playerAttributes;

    @NotNull
    private List<InventoryItemEntry> inventory;

    public FixedConfig(@NotNull RandomDistribution spawnShiftRange, @NotNull RandomDistribution spawnYHeightDistribution, @NotNull Map<String, Float> playerAttributes, @NotNull List<InventoryItemEntry> inventory) {
        this.spawnShiftRange = spawnShiftRange;
        this.spawnYHeightDistribution = spawnYHeightDistribution;
        this.playerAttributes = playerAttributes;
        this.inventory = inventory;
    }

    @NotNull
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

    @NotNull
    public Set<Item> getRequiredItems() {
        return inventory
                .stream()
                .map(InventoryItemEntry::getItem)
                .collect(Collectors.toSet());
    }
}
