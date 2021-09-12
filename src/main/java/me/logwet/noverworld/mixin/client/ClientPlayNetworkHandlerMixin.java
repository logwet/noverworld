package me.logwet.noverworld.mixin.client;

import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.NoverworldClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("TAIL"), method = "onGameJoin")
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        NoverworldClient.saveOldOptions();
        if (Noverworld.isNewWorld()) {
            Noverworld.log(Level.INFO, "Connected Clientside");
            NoverworldClient.onClientJoin();
        } else {
            Noverworld.log(Level.INFO, "World is not being created for the first time, Noverworld will not run");
        }
    }
}
