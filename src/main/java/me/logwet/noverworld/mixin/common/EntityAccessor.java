package me.logwet.noverworld.mixin.common;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("netherPortalCooldown")
    void setNetherPortalCooldown(int netherPortalCooldown);
}
