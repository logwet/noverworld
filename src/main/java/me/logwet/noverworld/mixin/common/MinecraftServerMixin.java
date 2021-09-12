package me.logwet.noverworld.mixin.common;

import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.NoverworldClient;
import me.logwet.noverworld.mixin.client.MinecraftClientAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Environment(EnvType.CLIENT)
    @Inject(at = @At("HEAD"), method = "prepareStartRegion")
    private void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        Noverworld.setNewWorld(((MinecraftClientAccessor) NoverworldClient.getMC()).getWorldGenProgressTracker().get().getProgressPercentage() != 100);
    }

    @Environment(EnvType.SERVER)
    @Inject(at = @At("HEAD"), method = "prepareStartRegion")
    private void prepareStartRegionDedicatedServer(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        Noverworld.setNewWorld(((WorldGenerationProgressLogger) worldGenerationProgressListener).getProgressPercentage() != 100);
    }
}
