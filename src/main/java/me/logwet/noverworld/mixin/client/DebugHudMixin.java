package me.logwet.noverworld.mixin.client;

import me.logwet.noverworld.Noverworld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    /**
     * @author DuncanRuns
     * @reason Puts mod notice in F3 menu
     */
    @Inject(at=@At("RETURN"), method="getLeftText", cancellable = true)
    private void addDebugLineMixin(CallbackInfoReturnable<List<String>> info) {
        info.getReturnValue().add("Noverworld mod v" + Noverworld.VERSION + " by logwet");
    }
}
