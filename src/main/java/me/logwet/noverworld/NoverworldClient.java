package me.logwet.noverworld;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class NoverworldClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		Noverworld.logger.info("Using Noverworld mod by logwet!");
		Noverworld.setMC(MinecraftClient.getInstance());
	}
}
