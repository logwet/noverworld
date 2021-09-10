package me.logwet.noverworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.logwet.noverworld.config.*;
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

	public static final Path CONFIG_FILE_PATH = Paths.get("config/noverworld.json").toAbsolutePath();

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

	public static MinecraftClient getMC() {
		return MC;
	}

	public static IntegratedServer getMS() {
		return getMC().getServer();
	}

	public static ServerWorld getNether() {
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

	public static void resetRandoms(long seed) {
		randomInstance = new Random(seed);
		int i;
		for (i=0; i<22; i++) {
			// This scrambling is intentional. It's there to combat determining info about the stronghold from the yaw/spawn height à la divine travel.
			randomInstance.nextInt();
		}
		spawnYHeightSets = new WeightedCollection<>(randomInstance);
		spawnYHeightSets.add(80, IntStream.range(7,13).toArray());
		spawnYHeightSets.add(5, IntStream.range(14,59).toArray());
		spawnYHeightSets.add(10, IntStream.range(60,75).toArray());
		spawnYHeightSets.add(5, IntStream.range(76,90).toArray());

		log(Level.INFO, "Reset randoms using world seed");
	}

	private static int getSpawnYHeight() {
		int[] heightSet = spawnYHeightSets.next();
		return heightSet[randomInstance.nextInt(heightSet.length)];
	}

	private static Map<String, int[]> uniqueFixedConfigItems;
	private static List<NonUniqueItem> nonUniqueFixedConfigItems;

	private static void readFixedConfigs() {
		fixedConfig = new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(
				Noverworld.class.getResourceAsStream("/static_inventory.json"))), FixedConfig.class);
		uniqueFixedConfigItems = fixedConfig.getUniqueItems();
		nonUniqueFixedConfigItems = fixedConfig.getNonUniqueItems();

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
		readFixedConfigs();
		try {
			readConfig();
			if (config.getItems().size() != uniqueFixedConfigItems.size()) {
				throw new MalformedConfigException("Config inventory length is wrong!");
			}
		} catch (Exception e) {
			log(Level.WARN, "Config file not found, new one being written to config/noverworld.json");
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
		itemStack.setCount(itemAttributes[0]);
		itemStack.setDamage(itemAttributes[1]);

		getServerPlayerEntity().inventory.setStack(itemAttributes[2], itemStack);
		getClientPlayerEntity().inventory.setStack(itemAttributes[2], itemStack);
	}

	public static void setPlayerInventory() {
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

			applyItemStack(itemStack, uniqueFixedConfigItems.get(nonUniqueItem.getName()));
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

	public static void sendToNether() {
		// The precision drop here is intentional. It's there to combat determining info about the stronghold from the yaw à la divine travel.
		getServerPlayerEntity().yaw = (float)Math.floor((-180f + randomInstance.nextFloat() * 360f) * 100) / 100;

		BlockPos oldPos = getServerPlayerEntity().getBlockPos();
		int yHeight = getSpawnYHeight();
		BlockPos pos = new BlockPos(oldPos.getX(), yHeight, oldPos.getY());

		getServerPlayerEntity().setPos(pos.getX(), pos.getY(), pos.getZ());
		getServerPlayerEntity().setInNetherPortal(pos);

		log(Level.INFO, "Attemping spawn at y " + yHeight + " with yaw " + getServerPlayerEntity().yaw);

		getServerPlayerEntity().changeDimension(getNether());
		getServerPlayerEntity().netherPortalCooldown = getServerPlayerEntity().getDefaultNetherPortalCooldown();

		log(Level.INFO, "Sent to nether");
	}

	public static void setHud() {
		getMC().options.debugEnabled = true;
//		getMC().options.debugProfilerEnabled = true;

		// This doesn't work/is unreliable and I'm not quite sure why.
		// getMC().openScreen(new GameMenuScreen(true));
		// getMC().getSoundManager().pauseAll();

		log(Level.INFO, "Opened F3 menu");
	}

	@Override
	public void onInitialize() {

	}
}
