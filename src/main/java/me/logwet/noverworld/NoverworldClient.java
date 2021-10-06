package me.logwet.noverworld;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.Option;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import org.apache.logging.log4j.Level;

import java.util.Objects;

public class NoverworldClient implements ClientModInitializer {
	private static MinecraftClient MC;
	private static double oldRenderDistance;
	private static double oldFOV;

	public static MinecraftClient getMC() {
        return MC;
    }

	public static void setMC(MinecraftClient mc) {
        MC = mc;
    }

	public static ClientPlayerEntity getClientPlayerEntity() {
        return getMC().player;
    }

	public static void saveOldOptions() {
		oldRenderDistance = Option.RENDER_DISTANCE.get(getMC().options);
		oldFOV = Option.FOV.get(getMC().options);

		Noverworld.log(Level.INFO, "Saved Render Distance " + oldRenderDistance + " and FOV " + oldFOV);
	}

	public static void resetOptions() {
		Option.RENDER_DISTANCE.set(getMC().options, oldRenderDistance);
		Option.FOV.set(getMC().options, oldFOV);

		Noverworld.log(Level.INFO, "Reset to Render Distance " + oldRenderDistance + " and FOV " + oldFOV);
	}

	private static void openF3() {
		try {
			if (Noverworld.config.isF3Enabled()) {
				getMC().options.debugEnabled = true;
				Noverworld.log(Level.INFO, "Opened F3 menu");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void onClientJoin() {
		openF3();
		Noverworld.log(Level.INFO, "Finished client side actions");
	}

	@Override
	public void onInitializeClient() {
		Noverworld.log(Level.INFO, "Using Noverworld v" + Noverworld.VERSION + " by logwet!");

		setMC(MinecraftClient.getInstance());

		Noverworld.commonConfigHandler();
	}
}
