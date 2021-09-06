package me.logwet.netherspawn_any_percent;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.HotbarStorageEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class NetherSpawnAnyPercent implements ModInitializer {

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

	private static HotbarStorageEntry getHotbar() {
		return getMC().getCreativeHotbarStorage().getSavedHotbar(8);
	}

	private static void setClientHotbar(HotbarStorageEntry hb) {
		assert getClientPlayerEntity() != null;
		int j;
		for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
			ItemStack itemStack = ((ItemStack)hb.get(j)).copy();
			getClientPlayerEntity().inventory.setStack(j, itemStack);
		}

		getClientPlayerEntity().playerScreenHandler.sendContentUpdates();
	}

	private static void setServerHotbar(HotbarStorageEntry hb) {
		int j;
		for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
			ItemStack itemStack = ((ItemStack)hb.get(j)).copy();
			getServerPlayerEntity().inventory.setStack(j, itemStack);
		}
	}

	public static void setHotbars() {
		HotbarStorageEntry hb = getHotbar();
		setClientHotbar(hb);
		setServerHotbar(hb);
	}

	public static void goNether() {
		getServerPlayerEntity().setInNetherPortal(getServerPlayerEntity().getBlockPos());
		getServerPlayerEntity().changeDimension(getNether());
		getServerPlayerEntity().netherPortalCooldown = getServerPlayerEntity().getDefaultNetherPortalCooldown();
	}

	public static void setHud() {
		getMC().options.debugEnabled = true;
		getMC().options.debugProfilerEnabled = true;

		// This doesn't work/is unreliable and I'm not quite sure why.
		//getMC().openScreen(new GameMenuScreen(true));
		//getMC().getSoundManager().pauseAll();

	}

	@Override
	public void onInitialize() {
//		System.out.println("Hello Fabric SERVER!");
	}


}
