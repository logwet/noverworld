package me.logwet.noverworld.mixin.common;

import me.logwet.noverworld.Noverworld;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (Noverworld.isNewWorld()) {
            Noverworld.log(Level.INFO, "Player " + player.getEntityName() + " connected and recognised");

            try {
                Noverworld.manageConfigs();
            } catch (Exception e) {
                Noverworld.log(Level.FATAL, "Unable to initialize Config. This is a fatal error, please make a report on the GitHub.");
                e.printStackTrace();
            }

            Noverworld.onServerJoin(player);
        } else {
            Noverworld.log(Level.INFO, "World is not being created for the first time, Noverworld will not run");
        }
    }
}
