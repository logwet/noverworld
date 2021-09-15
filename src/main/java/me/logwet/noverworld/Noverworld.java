package me.logwet.noverworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.logwet.noverworld.config.*;
import me.logwet.noverworld.mixin.common.HungerManagerAccessor;
import me.logwet.noverworld.mixin.common.ServerPlayerEntityAccessor;
import me.logwet.noverworld.mixin.common.StructureManagerInvoker;
import me.logwet.noverworld.returntooverworld.MagmaEntryHandler;
import me.logwet.noverworld.util.ItemsMapping;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Wearable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static Random randomInstance;
    private static float spawnYaw;
    private static Map<String, int[]> uniqueFixedConfigItems;
    private static List<NonUniqueItem> nonUniqueFixedConfigItems;
    private static Map<String, Float> playerAttributes;

    private static final String[] portalStructureNames = new String[]{"portal_1"};
    private static final Map<String, Structure> portalStructureMap = new HashMap<>();
    private static BlockPos portalPos;
    private static boolean naturalRTOFound = false;

    private static final AtomicBoolean featureHandlersActive = new AtomicBoolean(false);

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

    private static ServerWorld getOverworld() {
        return getMS().getOverworld();
    }

    private static ServerWorld getNether() {
        return getMS().getWorld(World.NETHER);
    }

    public static BlockPos getWorldSpawn() {
        return getOverworld().getSpawnPos();
    }

    public static ChunkPos getWorldSpawnChunk() {
        return new ChunkPos(getWorldSpawn());
    }

    public static boolean isFeatureHandlersActive() {
        return featureHandlersActive.get();
    }

    private static void setFeatureHandlersActive(boolean b) {
        featureHandlersActive.set(b);
    }

    private static Random newRandomInstance() {
        long rawSeed = Objects.requireNonNull(getOverworld().getSeed());
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

        Random returnRandom = new Random(seed);
        int j = returnRandom.nextInt(50) + 50;
        for (int i = 0; i < j; i++) {
            returnRandom.nextInt();
        }

        return returnRandom;
    }

    private static void resetRandoms() {
        randomInstance = newRandomInstance();

        spawnYaw = getRandomAngle();

        log(Level.INFO, "Reset randoms using world seed");
    }

    private static float getRandomAngle() {
        return (float) Math.floor((-180f + randomInstance.nextFloat() * 360f) * 100) / 100;
    }

    public static void readFixedConfigs() {
        fixedConfig = new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(
                Noverworld.class.getResourceAsStream("/fixed_config.json"))), FixedConfig.class);

        uniqueFixedConfigItems = fixedConfig.getUniqueItems();
        nonUniqueFixedConfigItems = fixedConfig.getNonUniqueItems();

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

    private static void filLArea(int x, int fx, int y, int fy, int z, int fz, Block replaceBlock, @Nullable Double chance, @Nullable Block replaceBlock2, @Nullable Boolean update) {
        Iterator<BlockPos> blockIterator = BlockPos.iterate(x, y, z, fx, fy, fz).iterator();

        BlockPos blockPos;

        Block changeBlock;

        while(blockIterator.hasNext()) {
            blockPos = blockIterator.next();

            BlockEntity blockEntity = getOverworld().getBlockEntity(blockPos);
            Clearable.clear(blockEntity);

            if (Objects.isNull(chance)) {
                changeBlock = replaceBlock;
            } else {
                float f = randomInstance.nextFloat();
                if ((double)f < chance) {
                    changeBlock = replaceBlock2;
                } else {
                    changeBlock = replaceBlock;
                }
            }

            getOverworld().setBlockState(blockPos, Objects.requireNonNull(changeBlock).getDefaultState());
            if (Boolean.TRUE.equals(update)) getOverworld().getBlockTickScheduler().schedule(blockPos, changeBlock, 0);

        }

        blockIterator = BlockPos.iterate(x, y, z, fx, fy, fz).iterator();
        while(blockIterator.hasNext()) {
            blockPos = blockIterator.next();
            getOverworld().updateNeighbors(blockPos, replaceBlock);
        }
    }

    private static void fillFakeWaterAccess(int x, int z){
        final Block replaceBlock = Blocks.WATER;
        int fx = x + MagmaEntryHandler.SEARCH_BOX_SIZE-1;
        int fz = z + MagmaEntryHandler.SEARCH_BOX_SIZE-1;
        int y = 13;
        int fy = 62;

        filLArea(x, fx, y, fy, z, fz, replaceBlock, null, null, null);
    }

    private static void fillFakePortalBase(int x, int z) {
        int fx = x + MagmaEntryHandler.SEARCH_BOX_SIZE-1;
        int fz = z + MagmaEntryHandler.SEARCH_BOX_SIZE-1;
        int y = 10;
        int fy = 10;

        filLArea(x, fx, y, fy, z, fz, Blocks.OBSIDIAN, 0.25D, Blocks.MAGMA_BLOCK, true);
        filLArea(x, fx, y+1, fy+2, z, fz, Blocks.WATER, null, null, null);
    }

    private static void genRTOPortal(@Nullable Integer x, @Nullable Integer z) {
        /*
         * Yeah I know this method of grabbing the schematic file is obscenely stupid, but trying to access it using
         * the inbuilt minecraft structure/resource managers just didn't work.
         */
        for (String structureName : portalStructureNames) {
            portalStructureMap.putIfAbsent(structureName,
                    ((StructureManagerInvoker) getMS().getStructureManager())
                            .invokeReadStructure(
                                    Noverworld.class.getResourceAsStream
                                            ("/data/noverworld/structures/" + structureName + ".nbt")
                            ));
        }

        String portalStructureName = Util.getRandom(portalStructureNames, randomInstance);
        Structure portalStructure = portalStructureMap.get(portalStructureName);

        BlockPos spawnPos = getWorldSpawn();

        if (Objects.isNull(x) || Objects.isNull(z)) {
            BlockPos foundBiomePos = null;
            final List<Biome> biomesToSearch = new ArrayList<>(Arrays.asList(
                    Biomes.DEEP_OCEAN,
                    Biomes.DEEP_COLD_OCEAN,
                    Biomes.DEEP_LUKEWARM_OCEAN,
                    Biomes.DEEP_FROZEN_OCEAN,
                    Biomes.OCEAN,
                    Biomes.COLD_OCEAN,
                    Biomes.LUKEWARM_OCEAN,
                    Biomes.WARM_OCEAN,
                    Biomes.FROZEN_OCEAN
            ));

            while (Objects.isNull(foundBiomePos)) {
                if (biomesToSearch.size() > 0) {
                    foundBiomePos = getOverworld().locateBiome(biomesToSearch.remove(0), spawnPos, 160, 8);
                } else {
                    foundBiomePos = spawnPos;
                }
            }

            x = foundBiomePos.getX();
            z = foundBiomePos.getZ();

            fillFakePortalBase(x, z);
        }

        BlockMirror mirror = Util.getRandom(BlockMirror.values(), randomInstance);
        BlockRotation rotation = BlockRotation.random(randomInstance);

        BlockPos portalPos = new BlockPos(x, 8, z);
        StructurePlacementData structurePlacementData = (new StructurePlacementData()).setMirror(mirror).setRotation(rotation).setIgnoreEntities(true).setChunkPosition((ChunkPos)null);

        BlockBox adjustedBoundingBox = portalStructure.calculateBoundingBox(structurePlacementData, portalPos);
        BlockPos adjustedPortalPos = portalPos.add(x-adjustedBoundingBox.minX, 0, z-adjustedBoundingBox.minZ);

        portalStructure.place(getOverworld(), adjustedPortalPos, structurePlacementData, randomInstance);
        fillFakeWaterAccess(x, z);

        int spawnShift = MagmaEntryHandler.SEARCH_BOX_SIZE/2-1;

        Noverworld.portalPos = Structure.transformAround(
                        new BlockPos(spawnShift, 9, spawnShift),
                        mirror, rotation, new BlockPos(0, 0, 0))
                .add(2*x-adjustedBoundingBox.minX, 0, 2*z-adjustedBoundingBox.minZ);

        log(Level.INFO, "Generated portal in overworld at " + portalPos.toShortString());
    }

    private static void sendToNether(ServerPlayerEntity serverPlayerEntity) {
        serverPlayerEntity.yaw = spawnYaw;

        serverPlayerEntity.setPos(portalPos.getX(), portalPos.getY(), portalPos.getZ());
        serverPlayerEntity.setInNetherPortal(portalPos);

        playerLog(Level.INFO, "Attemping spawn at " + portalPos.toShortString() + " with yaw " + serverPlayerEntity.yaw, serverPlayerEntity);

        serverPlayerEntity.changeDimension(getNether());
        serverPlayerEntity.netherPortalCooldown = serverPlayerEntity.getDefaultNetherPortalCooldown();

        if (!naturalRTOFound) {
            serverPlayerEntity.sendMessage(new LiteralText("Artificial RTO portal generated").formatted(Formatting.RED), true);
            serverPlayerEntity.sendMessage(new LiteralText("[Noverworld] Artificial RTO generated").formatted(Formatting.RED), false);
        } else {
            serverPlayerEntity.sendMessage(new LiteralText("Natural RTO portal generated!").formatted(Formatting.GREEN), true);
            serverPlayerEntity.sendMessage(new LiteralText("[Noverworld] Natural RTO portal generated").formatted(Formatting.GREEN), false);
        }

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

            setPlayerInventory(serverPlayerEntity);
            sendToNether(serverPlayerEntity);
            setPlayerAttributes(serverPlayerEntity);
            disableSpawnInvulnerability(serverPlayerEntity);

            playerLog(Level.INFO, "Finished server side actions", serverPlayerEntity);
        } else {
            playerLog(Level.INFO, "Noverworld will not handle player", serverPlayerEntity);
        }
    }

    public static void onWorldGenStart() {
        boolean worldIsNew = getOverworld().getTime() == 0;
        setNewWorld(worldIsNew);

        portalPos = null;
        naturalRTOFound = false;

        if (worldIsNew) {
            resetRandoms();

            setFeatureHandlersActive(true);
            MagmaEntryHandler.reset();
        }
        log(Level.INFO, worldIsNew ? "Detected creation of a new world" : "Detected reopening of a previously created world");
    }

    public static void onWorldGenComplete() {
        setFeatureHandlersActive(false);

        if (isNewWorld()) {
            log(Level.INFO, "World gen is complete");

            portalPos = null;
            naturalRTOFound = false;

            try {
                if (MagmaEntryHandler.ifFoundViableBlocks()) {
                    log(Level.INFO, "Found " + MagmaEntryHandler.getViableBlockCount() + " magma ravine blocks");

                    int[] foundPortalPos = MagmaEntryHandler.searchForSuitableArea();
                    if (naturalRTOFound = !Objects.isNull(foundPortalPos)) {
                        log(Level.INFO, "Found portal pos at " + Arrays.toString(foundPortalPos));
                        genRTOPortal(foundPortalPos[0], foundPortalPos[1]);
                        return;
                    }
                }

                log(Level.INFO, "Unable to find natural RTO portal, generating artificial one");
                genRTOPortal(null, null);
            } catch (Exception e) {
                e.printStackTrace();
                portalPos = getWorldSpawn();
            }
        }
    }
}
