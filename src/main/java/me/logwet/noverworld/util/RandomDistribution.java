package me.logwet.noverworld.util;

import java.util.HashMap;
import java.util.Random;
import java.util.stream.IntStream;

public class RandomDistribution extends HashMap<String, Integer> {
    private WeightedCollection<int[]> weightedCollection;

    public void createDistribution(Random randomInstance) {
        weightedCollection = new WeightedCollection<>(randomInstance);

        this.forEach((key, value) -> {
            String[] range = key.split("-");
            weightedCollection.add(value, IntStream.range(Integer.parseInt(range[0]), Integer.parseInt(range[1])).toArray());
        });
    }

    public int getNext(Random randomInstance) {
        int[] values = weightedCollection.next();
        return values[randomInstance.nextInt(values.length)];
    }
}
