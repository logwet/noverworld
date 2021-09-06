package me.logwet.netherspawn_any_percent.mixin;

import me.logwet.netherspawn_any_percent.NetherSpawnAnyPercent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("TAIL"), method = "onGameJoin")
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        NetherSpawnAnyPercent.setHotbars();
        System.out.println("Nether Spawn Any%: Loaded first creative hotbar");
        NetherSpawnAnyPercent.goNether();
        System.out.println("Nether Spawn Any%: Sent to nether");
    }
}
