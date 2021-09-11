package me.logwet.noverworld.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {
    @Accessor("joinInvulnerabilityTicks")
    public void setJoinInvulnerabilityTicks(int joinInvulnerabilityTicks);
}
