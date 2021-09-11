package me.logwet.noverworld.mixin;

import me.logwet.noverworld.Noverworld;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("TAIL"), method = "onGameJoin")
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (Noverworld.isNewWorld()) {
            Noverworld.log(Level.INFO, "Creation of new world detected");

            try {
                Noverworld.manageConfigs();
            } catch (Exception e) {
                Noverworld.log(Level.FATAL, "Unable to initialize Config. This is a fatal error, please make a report on the GitHub.");
                e.printStackTrace();
            }

            Noverworld.resetRandoms();
            Noverworld.setPlayerInventory();
            Noverworld.sendToNether();
            Noverworld.disableSpawnInvulnerability();
            Noverworld.setHud();
        }
    }
}
