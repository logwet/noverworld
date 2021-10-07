package me.logwet.noverworld.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Map<String, Integer> getItems() {
        return inventory
                .stream()
                .collect(Collectors.toMap(InventoryItemEntry::getName, InventoryItemEntry::getSlot));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof List && (((List<Object>) o).size() != 0 && ((List<Object>) o).get(0) instanceof FixedConfigInventoryItemEntry)) {
            Stream<String> uniqueItems = ((List<FixedConfigInventoryItemEntry>) o).stream().map(FixedConfigInventoryItemEntry::getName);

            return getInventory()
                    .stream()
                    .map(InventoryItemEntry::getName)
                    .allMatch(userConfigItem -> (uniqueItems.anyMatch(userConfigItem::equals)));
        }
        return false;
    }
}

