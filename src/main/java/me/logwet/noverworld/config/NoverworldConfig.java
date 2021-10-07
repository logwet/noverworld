package me.logwet.noverworld.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NoverworldConfig {
    private boolean f3Enabled = true;

    private boolean recipeBookEnabled = true;

    private List<UserConfigInventoryItemEntry> inventory;

    public NoverworldConfig() {
        inventory = new ArrayList<>();
    }

    public boolean isF3Enabled() {
        return f3Enabled;
    }

    public boolean isRecipeBookEnabled() {
        return recipeBookEnabled;
    }

    public List<UserConfigInventoryItemEntry> getInventory() {
        return inventory;
    }

    public void setInventory(List<UserConfigInventoryItemEntry> inventory) {
        this.inventory = inventory;
    }

    public Map<String, Integer> getItems() {
        return inventory
                .stream()
                .collect(Collectors.toMap(UserConfigInventoryItemEntry::getName, UserConfigInventoryItemEntry::getSlot));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof List && (((List<Object>) o).size() != 0 && ((List<Object>) o).get(0) instanceof InventoryItemEntry)) {
            Set<String> uniqueItems = ((List<InventoryItemEntry>) o)
                    .stream()
                    .map(InventoryItemEntry::getName)
                    .collect(Collectors.toSet());
            return getInventory()
                    .stream()
                    .map(UserConfigInventoryItemEntry::getName)
                    .map(String::toUpperCase)
                    .allMatch(uniqueItems::contains);
        }
        return false;
    }
}

