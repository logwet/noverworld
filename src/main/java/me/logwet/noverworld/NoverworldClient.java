package me.logwet.noverworld;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.Level;

public class NoverworldClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Noverworld.log(Level.INFO, "Using Noverworld v" + Noverworld.VERSION + " by logwet!");
		Noverworld.setMC(MinecraftClient.getInstance());

		try {
			Noverworld.manageConfigs();
			Noverworld.log(Level.INFO, "Initialized Config");
		} catch (Exception e) {
			Noverworld.log(Level.FATAL, "Unable to initialize Config. This is a fatal error, please make a report on the GitHub.");
			e.printStackTrace();
		}

	}
}
