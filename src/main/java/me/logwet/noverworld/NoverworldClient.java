package me.logwet.noverworld;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class NoverworldClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		System.out.println("Using Noverworld mod!");
		Noverworld.setMC(MinecraftClient.getInstance());
	}
}
