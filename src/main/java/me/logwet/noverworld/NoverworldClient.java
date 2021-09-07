package me.logwet.noverworld;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.Level;

public class NoverworldClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Noverworld.log(Level.INFO, "Using Noverworld v" + Noverworld.VERSION + " by logwet!");
		Noverworld.setMC(MinecraftClient.getInstance());
	}
}
