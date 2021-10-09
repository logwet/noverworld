package me.logwet.noverworld.util;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author Ross Paffett (https://gist.github.com/raws/1667807)
 * Modified to minimise calls to Random.nextInt()
 */

public class WeightedCollection<E> {

    private final NavigableMap<Integer, E> map = new TreeMap<Integer, E>();
    private final Random random;
    private int total = 0;

    public WeightedCollection() {
        this(new Random());
    }

    public WeightedCollection(Random random) {
        this.random = random;
    }

    public void add(int weight, E object) {
        assert weight >= 0;
        total += weight;
        map.put(total, object);
    }

    public E next() {
        int value;
        if (map.size() > 1) {
            value = random.nextInt(total) + 1; // Can also use floating-point weights
        } else {
            value = total;
        }
        return map.ceilingEntry(value).getValue();
    }
}
