package me.logwet.noverworld.config;

public class NonUniqueItem {
    private String name;
    private int[] attributes;

    public NonUniqueItem(String name, int[] attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public int[] getAttributes() {
        return attributes;
    }
}
