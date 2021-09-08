package me.logwet.noverworld;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.HotbarStorage;
import net.minecraft.client.options.HotbarStorageEntry;
import net.minecraft.client.options.Option;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.stream.IntStream;

public class Noverworld implements ModInitializer {
	public static final String VERSION = FabricLoader.getInstance().getModContainer("noverworld").get().getMetadata().getVersion().getFriendlyString();
	private static final Logger logger = LogManager.getLogger("Noverworld");

	public static void log(Level level, String message) {
		logger.log(level, "[Noverworld] " + message);
	}


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

	private static HotbarStorage getHotbarStorage() {
		return getMC().getCreativeHotbarStorage();
	}

	private static HotbarStorageEntry getHotbar(int i) {
		return getHotbarStorage().getSavedHotbar(i);
	}

	private static int hotbarSize() {
		return PlayerInventory.getHotbarSize();
	}

	private static void setClientHotbar(HotbarStorageEntry hb) {
		assert getClientPlayerEntity() != null;
		int j;
		for(j = 0; j < hotbarSize(); ++j) {
			ItemStack itemStack = ((ItemStack)hb.get(j)).copy();
			getClientPlayerEntity().inventory.setStack(j, itemStack);
		}

		getClientPlayerEntity().playerScreenHandler.sendContentUpdates();
	}

	private static void setServerHotbar(HotbarStorageEntry hb) {
		int j;
		for(j = 0; j < hotbarSize(); ++j) {
			ItemStack itemStack = ((ItemStack)hb.get(j)).copy();
			getServerPlayerEntity().inventory.setStack(j, itemStack);
		}
	}

	public static void setHotbars() {
		HotbarStorageEntry hb = getHotbar(8);
		setClientHotbar(hb);
		setServerHotbar(hb);
		log(Level.INFO, "Loaded ninth creative hotbar");
	}

	public static void saveDefaultHotbars() {
		final ItemStack[][] hotbars = {
				{
						new ItemStack(Items.WOODEN_AXE),
						new ItemStack(Items.IRON_PICKAXE),
						new ItemStack(Items.IRON_SHOVEL),
						new ItemStack(Items.LAVA_BUCKET),
						new ItemStack(Items.OAK_PLANKS, 22),
						new ItemStack(Items.BREAD, 8),
						new ItemStack(Items.OAK_BOAT),
						new ItemStack(Items.CRAFTING_TABLE),
						new ItemStack(Items.FLINT_AND_STEEL)
				}
		};

		hotbars[0][0].setDamage(10);

		int i;
		int j;

		for(i = 0; i < hotbars.length; i++) {
			int normedIndex = (hotbarSize() - hotbars.length) + i;
			HotbarStorageEntry hb = getHotbar(normedIndex);
			int k = 0;

			for(j = 0; j < hotbarSize(); ++j) {
				ItemStack itemStack = ((ItemStack)hb.get(j)).copy();
				if (itemStack.isEmpty()) {
					k++;
				}
			}

			if (k == hotbarSize()) {
				for(j = 0; j < hotbarSize(); ++j) {
					hb.set(j, hotbars[i][j].copy());
				}
				getHotbarStorage().save();

//				Text text = MC.options.keysHotbar[normedIndex].getBoundKeyLocalizedText();
//				Text text2 = MC.options.keyLoadToolbarActivator.getBoundKeyLocalizedText();

				log(Level.INFO, "Saved default hotbar to slot " + (normedIndex+1));
			}
		}
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
