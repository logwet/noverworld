// https://gist.github.com/raws/1667807

package me.logwet.netherspawn_any_percent;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedCollection<E> {

    private NavigableMap<Integer, E> map = new TreeMap<Integer, E>();
    private Random random;
    private int total = 0;

    public WeightedCollection() {
        this(new Random());
    }

    public WeightedCollection(Random random) {
        this.random = random;
    }

    public void add(int weight, E object) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, object);
    }

    public E next() {
        int value = random.nextInt(total) + 1; // Can also use floating-point weights
        return map.ceilingEntry(value).getValue();
    }

}
