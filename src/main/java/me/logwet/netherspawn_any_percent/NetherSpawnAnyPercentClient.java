package me.logwet.netherspawn_any_percent;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class NetherSpawnAnyPercentClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		System.out.println("Using Nether Spawn Any% mod!");
		NetherSpawnAnyPercent.setMC(MinecraftClient.getInstance());
	}
}
