package me.logwet.noverworld;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.HotbarStorage;
import net.minecraft.client.options.HotbarStorageEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.stream.IntStream;

public class Noverworld implements ModInitializer {
	public static final Logger logger = LogManager.getLogger("Noverworld");

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
		return MC.getServer();
	}

	public static ServerWorld getNether() {
		return getMS().getWorld(World.NETHER);
	}

	private static ClientPlayerEntity getClientPlayerEntity() {
		return MC.player;
	}

	private static String getPlayerName() {
		assert getClientPlayerEntity() != null;
		return getClientPlayerEntity().getName().asString();
	}

	private static ServerPlayerEntity getServerPlayerEntity() {
		return getMS().getPlayerManager().getPlayer(getPlayerName());
	}

	private static final WeightedCollection<int[]> spawnYHeightSets = new WeightedCollection<>();
	private static final Random randomInstance = new Random();

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
		logger.info("Loaded ninth creative hotbar");
	}

	public static void saveDefaultHotbars() {
		final ItemStack[][] hotbars = {
				{
						new ItemStack(Items.IRON_AXE),
						new ItemStack(Items.IRON_PICKAXE),
						new ItemStack(Items.IRON_SHOVEL),
						new ItemStack(Items.LAVA_BUCKET),
						new ItemStack(Items.OAK_PLANKS, 16),
						new ItemStack(Items.BREAD, 8),
						new ItemStack(Items.OAK_BOAT),
						new ItemStack(Items.CRAFTING_TABLE),
						new ItemStack(Items.FLINT_AND_STEEL)
				}
		};

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

				logger.info("Saved default hotbar to slot " + (normedIndex+1));
			}
		}
	}

	public static void sendToNether() {
		BlockPos oldPos = getServerPlayerEntity().getBlockPos();
		int yHeight = getSpawnYHeight();
		BlockPos pos = new BlockPos(oldPos.getX(), yHeight, oldPos.getY());

		getServerPlayerEntity().setInNetherPortal(pos);

		getServerPlayerEntity().yaw = -180f + randomInstance.nextFloat() * 360f;

		logger.info("Attemping spawn at y " + yHeight + " with yaw " + getServerPlayerEntity().yaw);

		getServerPlayerEntity().changeDimension(getNether());
		getServerPlayerEntity().netherPortalCooldown = getServerPlayerEntity().getDefaultNetherPortalCooldown();

		logger.info("Sent to nether");
	}

	public static void setHud() {
		getMC().options.debugEnabled = true;
		getMC().options.debugProfilerEnabled = true;

		// This doesn't work/is unreliable and I'm not quite sure why.
		// getMC().openScreen(new GameMenuScreen(true));
		// getMC().getSoundManager().pauseAll();

		logger.info("Opened f3 menu");
	}

	@Override
	public void onInitialize() {
		spawnYHeightSets.add(80, IntStream.range(7,13).toArray());
		spawnYHeightSets.add(5, IntStream.range(14,59).toArray());
		spawnYHeightSets.add(10, IntStream.range(60,75).toArray());
		spawnYHeightSets.add(5, IntStream.range(76,90).toArray());
	}
}
