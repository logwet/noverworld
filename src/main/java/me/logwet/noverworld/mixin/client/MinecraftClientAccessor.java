package me.logwet.noverworld.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("worldGenProgressTracker")
    AtomicReference<WorldGenerationProgressTracker> getWorldGenProgressTracker();
}
