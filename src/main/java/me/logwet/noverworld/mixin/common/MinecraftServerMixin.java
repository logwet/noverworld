package me.logwet.noverworld.mixin.common;

import me.logwet.noverworld.Noverworld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;",
                    shift = At.Shift.AFTER
            ),
            method = "prepareStartRegion"
    )
    private void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        Noverworld.onWorldGenStart();
    }

    @Inject(at = @At("TAIL"), method = "loadWorld")
    private void loadWorld(CallbackInfo ci) {
        Noverworld.onWorldGenComplete();
    }
}
