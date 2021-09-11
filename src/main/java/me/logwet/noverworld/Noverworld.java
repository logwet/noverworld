package me.logwet.noverworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.logwet.noverworld.config.*;
import me.logwet.noverworld.mixin.HungerManagerAccessor;
import me.logwet.noverworld.mixin.ServerPlayerEntityAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.Option;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Wearable;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

public class Noverworld implements ModInitializer {
	public static final String VERSION = FabricLoader.getInstance().getModContainer("noverworld").get().getMetadata().getVersion().getFriendlyString();

//	I would like to use this first implementation as it is the suggested and recommended way of doing things with fabric.
//	Unfortunately, it has strange behaviour in my dev environment I don't have the time to trouble shoot
//	public static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("noverworld.json");

	public static final Path CONFIG_FILE_PATH = Paths.get("config/noverworld-" + VERSION + ".json").toAbsolutePath();

	private static final Logger logger = LogManager.getLogger("Noverworld");

	public static void log(Level level, String message) {
		logger.log(level, "[Noverworld] " + message);
	}

	private static NoverworldConfig config;

	private static FixedConfig fixedConfig;

	private static boolean newWorld = false;

	public static boolean isNewWorld() {
		return newWorld;
	}

	public static void setNewWorld(boolean newWorld) {
		Noverworld.newWorld = newWorld;
	}

	private static MinecraftClient MC;

	public static void setMC(MinecraftClient mc) {
		MC = mc;
	}

	private static MinecraftClient getMC() {
		return MC;
	}

	private static IntegratedServer getMS() {
		return getMC().getServer();
	}

	private static ServerWorld getNether() {
		return getMS().getWorld(World.NETHER);
	}

	private static ClientPlayerEntity getClientPlayerEntity() {
		return getMC().player;
	}

	private static String getPlayerName() {
		assert getClientPlayerEntity() != null;
		return getClientPlayerEntity().getName().asString();
	}

	private static ServerPlayerEntity getServerPlayerEntity() {
		return getMS().getPlayerManager().getPlayer(getPlayerName());
	}

	private static Random randomInstance;
	private static WeightedCollection<int[]> spawnYHeightSets;

	private static void resetRandoms() {
		long rawSeed = Objects.requireNonNull(Noverworld.getMS().getWorld(World.OVERWORLD)).getSeed();
		String rawSeedString = Long.toString(rawSeed);
		long seed;
		StringBuilder seedString = new StringBuilder();

		/*
		 This drops every second digit from the world seed and uses the result as the random seed for all RNG in the mod
		 It's a measure to combat a potential divine travel esque situation.
		 */
		for (int i=0; i < rawSeedString.length(); i += 2) {
			seedString.append(rawSeedString.charAt(i));
			seedString.append("0");
		}

		try {
			seed = Long.parseLong(seedString.toString());
		} catch (NumberFormatException e) {
			log(Level.INFO, "Unable to drop digits from seed. Using complete world seed.");
			seed = rawSeed;
		}

		randomInstance = new Random(seed);

		spawnYHeightSets = new WeightedCollection<>(randomInstance);

		spawnYHeightDistribution.forEach((rawRange, weight) -> {
			String[] stringRange = rawRange.split("-");
			int[] range = new int[]{Integer.parseInt(stringRange[0]), Integer.parseInt(stringRange[1])};
			spawnYHeightSets.add(weight, IntStream.range(range[0], range[1]).toArray());
		});

		log(Level.INFO, "Reset randoms using world seed");
	}

	private static int getSpawnYHeight() {
		int[] heightSet = spawnYHeightSets.next();
		return heightSet[randomInstance.nextInt(heightSet.length)];
	}

	private static float getRandomAngle() {
		return (float)Math.floor((-180f + randomInstance.nextFloat() * 360f) * 100) / 100;
	}

	private static Map<String, int[]> uniqueFixedConfigItems;
	private static List<NonUniqueItem> nonUniqueFixedConfigItems;
	private static int[] possibleSpawnShifts;
	private static Map<String, Integer> spawnYHeightDistribution;
	private static Map<String, Float> playerAttributes;

	public static void readFixedConfigs() {
		fixedConfig = new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(
				Noverworld.class.getResourceAsStream("/fixed_config.json"))), FixedConfig.class);

		uniqueFixedConfigItems = fixedConfig.getUniqueItems();
		nonUniqueFixedConfigItems = fixedConfig.getNonUniqueItems();

		possibleSpawnShifts = IntStream.range(fixedConfig.getSpawnShiftRange()[0], fixedConfig.getSpawnShiftRange()[1]).toArray();

		spawnYHeightDistribution = fixedConfig.getSpawnYHeightDistribution();

		playerAttributes = fixedConfig.getPlayerAttributes();

		ItemsMapping.readMappingsFromFile();
	}

	private static void readConfig() throws FileNotFoundException {
		config = new Gson().fromJson(new FileReader(CONFIG_FILE_PATH.toFile()), NoverworldConfig.class);
	}

	private static void saveConfig() {
		try {
			List<InventoryItemEntry> newConfigInventory = new ArrayList<>();
			uniqueFixedConfigItems.forEach((name, attributes) -> newConfigInventory.add(new InventoryItemEntry(name, attributes[2]+1)));
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

	public static void manageConfigs() throws FileNotFoundException {
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

	private static void applyItemStack(ItemStack itemStack, int[] itemAttributes) {
		if (itemStack.isStackable()) {
			itemStack.setCount(itemAttributes[0]);
		}
		if (itemStack.isDamageable()) {
			itemStack.setDamage(itemAttributes[1]);
		}

//		getServerPlayerEntity().inventory.setStack(itemAttributes[2], itemStack);
//		getClientPlayerEntity().inventory.setStack(itemAttributes[2], itemStack);

		getServerPlayerEntity().inventory.insertStack(itemAttributes[2], itemStack);
		getClientPlayerEntity().inventory.insertStack(itemAttributes[2], itemStack);
	}

	private static void setPlayerInventory() {
		assert getClientPlayerEntity() != null;

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
				applyItemStack(itemStack, itemAttributes);
			} else {
				log(Level.ERROR, "The item " + name + " cannot be configured!");
			}
		});

		nonUniqueFixedConfigItems.forEach(nonUniqueItem -> {
			ItemStack itemStack = getItemStackFromName(nonUniqueItem.getName());

			applyItemStack(itemStack, nonUniqueItem.getAttributes());
		});

		getClientPlayerEntity().playerScreenHandler.sendContentUpdates();

		log(Level.INFO, "Overwrote player inventory with configured items");
	}


	private static double oldRenderDistance;
	private static double oldFOV;

	public static void saveOldOptions() {
		oldRenderDistance = Option.RENDER_DISTANCE.get(getMC().options);
		oldFOV = Option.FOV.get(getMC().options);

		log(Level.INFO, "Saved Render Distance " + oldRenderDistance + " and FOV " + oldFOV);
	}

	public static void resetOptions() {
		Option.RENDER_DISTANCE.set(getMC().options, oldRenderDistance);
		Option.FOV.set(getMC().options, oldFOV);

		log(Level.INFO, "Reset to Render Distance " + oldRenderDistance + " and FOV " + oldFOV);
	}

	private static void sendToNether() {
		// The precision drop here is intentional. It's there to combat determining info about the stronghold from the yaw à la divine travel.
		getServerPlayerEntity().yaw = getRandomAngle();

		float spawnShiftAngle = getRandomAngle();
		float spawnShiftLength;

		try {
			spawnShiftLength = (float) possibleSpawnShifts[randomInstance.nextInt(possibleSpawnShifts.length)];
		} catch (Exception e) {
			spawnShiftLength = 0;
		}

		float spawnShiftAngleRadians = spawnShiftAngle * 0.017453292F;

		BlockPos oldPos = getServerPlayerEntity().getBlockPos();
		int yHeight = getSpawnYHeight();

		BlockPos pos = new BlockPos(
				oldPos.getX() - Math.round(spawnShiftLength * MathHelper.sin(spawnShiftAngleRadians)),
				yHeight,
				oldPos.getZ() + Math.round(spawnShiftLength * MathHelper.cos(spawnShiftAngleRadians))
		);

		getServerPlayerEntity().setPos(pos.getX(), pos.getY(), pos.getZ());
		getServerPlayerEntity().setInNetherPortal(pos);

		log(Level.INFO, "Spawn shifted " + spawnShiftLength + " blocks on yaw " + spawnShiftAngle);
		log(Level.INFO, "Attemping spawn at " + pos.toString() + " with yaw " + getServerPlayerEntity().yaw);

		getServerPlayerEntity().changeDimension(getNether());
		getServerPlayerEntity().netherPortalCooldown = getServerPlayerEntity().getDefaultNetherPortalCooldown();

		log(Level.INFO, "Sent to nether");
	}

	private static void disableSpawnInvulnerability() {
		((ServerPlayerEntityAccessor) getServerPlayerEntity()).setJoinInvulnerabilityTicks(0);
		log(Level.INFO, "Disabled spawn invulnerability");
	}

	private static void setPlayerAttributes() {
		getServerPlayerEntity().setHealth(playerAttributes.get("health"));
		getServerPlayerEntity().getHungerManager().setFoodLevel(Math.round(playerAttributes.get("hunger")));
		((HungerManagerAccessor) getServerPlayerEntity().getHungerManager()).setFoodSaturationLevel(playerAttributes.get("saturation"));
	}

	private static void setHud() {
		getMC().options.debugEnabled = true;
//		getMC().options.debugProfilerEnabled = true;

		// This doesn't work/is unreliable and I'm not quite sure why.
		// getMC().openScreen(new GameMenuScreen(true));
		// getMC().getSoundManager().pauseAll();

		log(Level.INFO, "Opened F3 menu");
	}

	public static void onSpawn() {
		resetRandoms();
		setPlayerInventory();
		sendToNether();
		setPlayerAttributes();
		disableSpawnInvulnerability();
		setHud();
	}

	@Override
	public void onInitialize() {

	}
}
