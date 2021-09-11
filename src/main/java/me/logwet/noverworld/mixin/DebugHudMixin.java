package me.logwet.noverworld.mixin;

import me.logwet.noverworld.Noverworld;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    /**
     * @author DuncanRuns
     * @reason Puts mod notice in F3 menu
     */
    @Inject(method="getLeftText", at=@At("RETURN"), cancellable = true)
    private void addDebugLineMixin(CallbackInfoReturnable<List<String>> info) {
        info.getReturnValue().add("Noverworld mod v" + Noverworld.VERSION + "by logwet");
    }
}
