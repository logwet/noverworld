package me.logwet.noverworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.logwet.noverworld.config.*;
import me.logwet.noverworld.mixin.common.HungerManagerAccessor;
import me.logwet.noverworld.mixin.common.ServerPlayerEntityAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Wearable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

public class Noverworld {
    public static final String VERSION = FabricLoader.getInstance().getModContainer("noverworld").get().getMetadata().getVersion().getFriendlyString();

//	I would like to use this first implementation as it is the suggested and recommended way of doing things with fabric.
//	Unfortunately, it has strange behaviour in my dev environment I don't have the time to trouble shoot
//	public static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("noverworld.json");

    public static final Path CONFIG_FILE_PATH = Paths.get("config/noverworld-" + VERSION + ".json").toAbsolutePath();

    public static final boolean IS_CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

    private static final Logger logger = LogManager.getLogger("Noverworld");
    public static NoverworldConfig config;
    private static FixedConfig fixedConfig;
    private static boolean newWorld = false;
    private static Set<UUID> initializedPlayers;
    private static MinecraftServer MS;
    private static Random playerRandomInstance;
    private static WeightedCollection<int[]> spawnYHeightSets;
    private static Map<String, int[]> uniqueFixedConfigItems;
    private static List<NonUniqueItem> nonUniqueFixedConfigItems;
    private static int[] possibleSpawnShifts;
    private static Map<String, Integer> spawnYHeightDistribution;
    private static Map<String, Float> playerAttributes;

    public static void log(Level level, String message) {
        logger.log(level, "[Noverworld] " + message);
    }

    public static void playerLog(Level level, String message, ServerPlayerEntity serverPlayerEntity) {
        log(level, "[" + serverPlayerEntity.getEntityName() + "] " + message);
    }

    public static boolean isNewWorld() {
        return newWorld;
    }

    public static void setNewWorld(boolean newWorld) {
        Noverworld.newWorld = newWorld;
    }

    public static Set<UUID> getInitializedPlayers() {
        return initializedPlayers;
    }

    public static void setInitializedPlayers(Set<UUID> initializedPlayers) {
        Noverworld.initializedPlayers = initializedPlayers;
    }

    public static MinecraftServer getMS() {
        return MS;
    }

    public static void setMS(MinecraftServer ms) {
        setInitializedPlayers(new HashSet<>());
        MS = ms;
    }

    private static ServerWorld getNether() {
        return getMS().getWorld(World.NETHER);
    }

    private static void resetRandoms() {
        long rawSeed = Noverworld.getMS().getOverworld().getSeed();
        String rawSeedString = Long.toString(rawSeed);
        long seed;
        StringBuilder seedString = new StringBuilder();

        /*
		 This drops every second digit from the world seed and uses the result as the random seed for all RNG in the mod
		 It's a measure to combat a potential divine travel esque situation.
		 */
        for (int i = 0; i < rawSeedString.length(); i += 2) {
            seedString.append(rawSeedString.charAt(i));
            seedString.append("0");
        }

        try {
            seed = Long.parseLong(seedString.toString());
        } catch (NumberFormatException e) {
            log(Level.INFO, "Unable to drop digits from seed. Using complete world seed.");
            seed = rawSeed;
        }

        playerRandomInstance = new Random(seed);

        spawnYHeightSets = new WeightedCollection<>(playerRandomInstance);

        spawnYHeightDistribution.forEach((rawRange, weight) -> {
            String[] stringRange = rawRange.split("-");
            int[] range = new int[]{Integer.parseInt(stringRange[0]), Integer.parseInt(stringRange[1])};
            spawnYHeightSets.add(weight, IntStream.range(range[0], range[1]).toArray());
        });

        log(Level.INFO, "Reset randoms using world seed");
    }

    private static int getSpawnYHeight() {
        int[] heightSet = spawnYHeightSets.next();
        return heightSet[playerRandomInstance.nextInt(heightSet.length)];
    }

    private static float getRandomAngle() {
        return (float) Math.floor((-180f + playerRandomInstance.nextFloat() * 360f) * 100) / 100;
    }

    public static void readFixedConfigs() {
        fixedConfig = new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(
                Noverworld.class.getResourceAsStream("/fixed_config.json"))), FixedConfig.class);

        uniqueFixedConfigItems = fixedConfig.getUniqueItems();
        nonUniqueFixedConfigItems = fixedConfig.getNonUniqueItems();

        possibleSpawnShifts = IntStream.range(fixedConfig.getSpawnShiftRange()[0], fixedConfig.getSpawnShiftRange()[1]).toArray();

        spawnYHeightDistribution = fixedConfig.getSpawnYHeightDistribution();

        playerAttributes = fixedConfig.getPlayerAttributes();

        ItemsMapping.readMappingsFromFile();

        log(Level.INFO, "Loaded fixed configs");
    }

    private static void readConfig() throws FileNotFoundException {
        config = new Gson().fromJson(new FileReader(CONFIG_FILE_PATH.toFile()), NoverworldConfig.class);
    }

    private static void saveConfig() {
        try {
            List<InventoryItemEntry> newConfigInventory = new ArrayList<>();
            uniqueFixedConfigItems.forEach((name, attributes) -> newConfigInventory.add(new InventoryItemEntry(name, attributes[2] + 1)));
            config = new NoverworldConfig();
            config.setInventory(newConfigInventory);

            PrintWriter writer = new PrintWriter(CONFIG_FILE_PATH.toFile());
            writer.print("");
            writer.close();

            Files.write(CONFIG_FILE_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(config).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void manageConfigs() throws FileNotFoundException {
        try {
            readConfig();
            if (config.getItems().size() != uniqueFixedConfigItems.size()) {
                throw new MalformedConfigException("Config inventory length is wrong!");
            }
        } catch (Exception e) {
            log(Level.WARN, "Config file not found, new one being written.");
            saveConfig();
            readConfig();
        }
    }

    public static void refreshConfigs() {
        try {
            manageConfigs();
        } catch (Exception e) {
            log(Level.FATAL, "Unable to initialize Config. This is a fatal error, please make a report on the GitHub.");
            e.printStackTrace();
        }
    }

    public static void commonConfigHandler() {
        try {
            readFixedConfigs();
            manageConfigs();
            log(Level.INFO, "Initialized Config");
        } catch (Exception e) {
            log(Level.FATAL, "Unable to initialize Config. This is a fatal error, please make a report on the GitHub.");
            e.printStackTrace();
        }
    }

    // Thank god for reflection ThankEgg
    private static ItemStack getItemStackFromName(String name) {
        Objects.requireNonNull(name);
        name = name.toUpperCase();
        try {
            String target;

            // Yes, this is very janky, yes, it also might be the best way to do it
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                target = FabricLoader.getInstance().getMappingResolver()
                        .mapFieldName("named", "net.minecraft.item.Items",
                                name, "net.minecraft.item.Item");
            } else {
                target = FabricLoader.getInstance().getMappingResolver()
                        .mapFieldName("intermediary", "net.minecraft.class_1802",
                                Objects.requireNonNull(ItemsMapping.getMappings().get(name)),
                                "net.minecraft.class_1792");
            }

            Field f = Items.class.getDeclaredField(target);

            return new ItemStack((Item) Objects.requireNonNull(f.get(null)));
        } catch (Exception e) {
            e.printStackTrace();
            log(Level.ERROR, "Unable to find the ItemStack " + name + ", please double check your config. Replaced with empty slot.");
            return ItemStack.EMPTY.copy();
        }
    }

    private static void applyItemStack(ItemStack itemStack, int[] itemAttributes, ServerPlayerEntity serverPlayerEntity) {
        if (itemStack.isStackable()) {
            itemStack.setCount(itemAttributes[0]);
        }
        if (itemStack.isDamageable()) {
            itemStack.setDamage(itemAttributes[1]);
        }

        serverPlayerEntity.inventory.insertStack(itemAttributes[2], itemStack);
    }

    private static void setPlayerInventory(ServerPlayerEntity serverPlayerEntity) {
        config.getItems().forEach((slot, name) -> {
            slot -= 1;
            if (uniqueFixedConfigItems.containsKey(name)) {
                ItemStack itemStack = getItemStackFromName(name);
                int[] fixedItemAttributes = uniqueFixedConfigItems.get(name);

                if (slot >= 36 && slot <= 39) {
                    if (!(itemStack.getItem() instanceof Wearable)) {
                        return; // Note, this doesn't make setPlayerInventory() return, it returns the current iteration of the forEach
                    }
                }

                int[] itemAttributes = new int[]{fixedItemAttributes[0], fixedItemAttributes[1], slot};
                applyItemStack(itemStack, itemAttributes, serverPlayerEntity);
            } else {
                playerLog(Level.ERROR, "The item " + name + " cannot be configured!", serverPlayerEntity);
            }
        });

        nonUniqueFixedConfigItems.forEach(nonUniqueItem -> {
            ItemStack itemStack = getItemStackFromName(nonUniqueItem.getName());

            applyItemStack(itemStack, nonUniqueItem.getAttributes(), serverPlayerEntity);
        });

        playerLog(Level.INFO, "Overwrote player inventory with configured items", serverPlayerEntity);
    }

    private static void sendToNether(ServerPlayerEntity serverPlayerEntity) {
        // The precision drop here is intentional. It's there to combat determining info about the stronghold from the yaw à la divine travel.
        serverPlayerEntity.yaw = getRandomAngle();

        float spawnShiftAngle = getRandomAngle();
        float spawnShiftLength;

        try {
            spawnShiftLength = (float) possibleSpawnShifts[playerRandomInstance.nextInt(possibleSpawnShifts.length)];
        } catch (Exception e) {
            spawnShiftLength = 0;
        }

        float spawnShiftAngleRadians = spawnShiftAngle * 0.017453292F;

        BlockPos oldPos = serverPlayerEntity.getBlockPos();
        int yHeight = getSpawnYHeight();

        BlockPos pos = new BlockPos(
                oldPos.getX() - Math.round(spawnShiftLength * MathHelper.sin(spawnShiftAngleRadians)),
                yHeight,
                oldPos.getZ() + Math.round(spawnShiftLength * MathHelper.cos(spawnShiftAngleRadians))
        );

        serverPlayerEntity.setPos(pos.getX(), pos.getY(), pos.getZ());
        serverPlayerEntity.setInNetherPortal(pos);

        playerLog(Level.INFO, "Spawn shifted " + spawnShiftLength + " blocks on yaw " + spawnShiftAngle, serverPlayerEntity);
        playerLog(Level.INFO, "Attemping spawn at " + pos + " with yaw " + serverPlayerEntity.yaw, serverPlayerEntity);

        serverPlayerEntity.changeDimension(getNether());
        serverPlayerEntity.netherPortalCooldown = serverPlayerEntity.getDefaultNetherPortalCooldown();

        playerLog(Level.INFO, "Sent to nether", serverPlayerEntity);
    }

    private static void disableSpawnInvulnerability(ServerPlayerEntity serverPlayerEntity) {
        ((ServerPlayerEntityAccessor) serverPlayerEntity).setJoinInvulnerabilityTicks(0);
        playerLog(Level.INFO, "Disabled spawn invulnerability", serverPlayerEntity);
    }

    private static void setPlayerAttributes(ServerPlayerEntity serverPlayerEntity) {
        if (playerAttributes.get("health") < 20.0F) {
            serverPlayerEntity.setHealth(playerAttributes.get("health"));
        }
        if (playerAttributes.get("hunger") < 20.0F) {
            serverPlayerEntity.getHungerManager().setFoodLevel(Math.round(playerAttributes.get("hunger")));
        }

        if (playerAttributes.get("saturation") < 20.0F) {
            ((HungerManagerAccessor) serverPlayerEntity.getHungerManager()).setFoodSaturationLevel(playerAttributes.get("saturation"));
        }
        playerLog(Level.INFO, "Set player attributes", serverPlayerEntity);
    }

    public static void onServerJoin(ServerPlayerEntity serverPlayerEntity) {
        if (isNewWorld() && getInitializedPlayers().add(serverPlayerEntity.getUuid())) {
            playerLog(Level.INFO, "Player connected and recognised", serverPlayerEntity);

            resetRandoms();
            setPlayerInventory(serverPlayerEntity);
            sendToNether(serverPlayerEntity);
            setPlayerAttributes(serverPlayerEntity);
            disableSpawnInvulnerability(serverPlayerEntity);

            playerLog(Level.INFO, "Finished server side actions", serverPlayerEntity);
        } else {
            playerLog(Level.INFO, "Noverworld will not handle player", serverPlayerEntity);
        }
    }
}
