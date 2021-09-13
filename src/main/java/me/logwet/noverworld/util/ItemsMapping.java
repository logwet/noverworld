package me.logwet.noverworld.util;

import me.logwet.noverworld.Noverworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 This whole mechanism is stupidly janky and I hate it but I don't think I have a choice. No way to get around the fact
 that named isn't accessible in a production environment.
 */
public class ItemsMapping {
    private static final Map<String, String> mappings = new HashMap<>();

    public static Map<String, String> getMappings() {
        return mappings;
    }

    public static void readMappingsFromFile() {
        String rawLine;
        String[] line;
        try (BufferedReader br = new BufferedReader(
                /*
                 Items.mapping contains the mappings from yarn-1.17.1+build.1, but because of the stable nature of
                 Intermediary mappings they will happily work cross versions.
                 Also if you're wondering how I got the list, I scraped the JavaDoc using Python and BS4.
                 No, https://github.com/FabricMC/yarn/blob/1.16.1/mappings/net/minecraft/item/Items.mapping does not work
                 because for some reason those mappings only include blocks, not items. In fact, I couldn't find items
                 (such as tools) anywhere on the GitHub repo. I'm not sure where the JavaDoc is sourcing its info from
                 */
                new InputStreamReader(
                        Objects.requireNonNull(Noverworld.class.getResourceAsStream("/Items.mapping"))))) {
            while ((rawLine = br.readLine()) != null) {
                line = rawLine.trim().split(" ");
                mappings.put(line[1], line[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
