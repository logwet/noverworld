package me.logwet.noverworld.mixin.client;

import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.NoverworldClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
//    @Inject(at = @At("HEAD"), method = "startIntegratedServer(Ljava/lang/String;Lnet/minecraft/util/registry/RegistryTracker$Modifiable;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V")
//    private void startIntegratedServer(String worldName, RegistryTracker.Modifiable registryTracker, Function<LevelStorage.Session, DataPackSettings> function, Function4<LevelStorage.Session, RegistryTracker.Modifiable, ResourceManager, DataPackSettings, SaveProperties> function4, boolean safeMode, MinecraftClient.WorldLoadAction worldLoadAction, CallbackInfo ci) {
//        Noverworld.setNewWorld(worldLoadAction == MinecraftClient.WorldLoadAction.CREATE);
//        Noverworld.saveOldOptions();
//    }

    @Inject(at = @At("TAIL"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    private void disconnect(Screen screen, CallbackInfo ci) {
        if (Noverworld.isNewWorld()) {
            NoverworldClient.resetOptions();
        }
    }
}
