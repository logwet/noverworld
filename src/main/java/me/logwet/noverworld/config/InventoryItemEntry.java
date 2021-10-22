package me.logwet.noverworld.config;

import me.logwet.noverworld.Noverworld;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public class InventoryItemEntry extends UserConfigInventoryItemEntry {
    @Nullable
    private String tags;

    @NotNull
    private String count;

    @Nullable
    private Integer damage;

    @NotNull
    private Boolean editable = false;

    @NotNull
    private Boolean unique = false;

    @Nullable
    private transient Item item;

    public InventoryItemEntry(@NotNull String name, @NotNull String count, Integer slot, @Nullable Integer damage, @NotNull Boolean editable, @NotNull Boolean unique) {
        super(name, slot);
        this.count = count;
        this.damage = damage;
        this.editable = editable;
        this.unique = unique;
    }

    @Nullable
    public String getTags() {
        return tags;
    }

    @NotNull
    public String getRawCount() {
        return count;
    }

    @NotNull
    public Integer getCount(@NotNull Random random) {
        String[] stringRange = getRawCount().split("-");
        int[] range = IntStream.range(Integer.parseInt(stringRange[0]), Integer.parseInt(stringRange[1])+1).toArray();
        if (range.length < 1) {
            return 0;
        }
        return range[random.nextInt(range.length)];
    }

    @Nullable
    public Integer getDamage() {
        return damage;
    }

    @NotNull
    public Integer getPrettySlot() {
        return slot + 1;
    }

    @NotNull
    public Boolean isEditable() {
        return editable;
    }

    @NotNull
    public Boolean isUnique() {
        return unique;
    }

    @Nullable
    private Item getItemStackFromName() {
//        String finalName = getName();
        try {
            return (Item) Registry.ITEM
                    .getOrEmpty(new Identifier(getName()))
                    .orElseThrow(() -> new ItemNotFoundException("Item " + getName() + " not found in registry!"));
        } catch (Exception e) {
            e.printStackTrace();
            Noverworld.log(Level.ERROR, "Unable to find the Item type " + getName() + ", please double check your config. Replaced with empty slot.");
            return null;
        }
    }

    @Nullable
    public Item getItem() {
        if (Objects.isNull(item)) {
            setItem(getItemStackFromName());
        }
        return item;
    }

    public void setItem(@Nullable Item item) {
        this.item = item;
    }
}
