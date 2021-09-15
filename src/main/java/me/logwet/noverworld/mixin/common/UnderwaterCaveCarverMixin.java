package me.logwet.noverworld.mixin.common;

import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.returntooverworld.MagmaEntryHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.UnderwaterCaveCarver;
import net.minecraft.world.gen.carver.UnderwaterRavineCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;
import java.util.Random;

@Mixin(UnderwaterCaveCarver.class)
public class UnderwaterCaveCarverMixin {
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Random;nextFloat()F"
            ),
            method = "carveAtPoint(Lnet/minecraft/world/gen/carver/Carver;Lnet/minecraft/world/chunk/Chunk;Ljava/util/BitSet;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos$Mutable;IIIIIIII)Z"
    )
    private static void genUnderwaterRavineCapBlock(Carver<?> carver, Chunk chunk, BitSet mask, Random random, BlockPos.Mutable pos, int seaLevel, int mainChunkX, int mainChunkZ, int x, int z, int relativeX, int y, int relativeZ, CallbackInfoReturnable<Boolean> cir) {
//        if (Noverworld.isFeatureHandlersActive() && MagmaEntryHandler.isActive() && carver instanceof UnderwaterRavineCarver) {
        if (Noverworld.isFeatureHandlersActive() && MagmaEntryHandler.isActive()) {
            ChunkPos spawnChunk = Noverworld.getWorldSpawnChunk();

            if ((mainChunkX >= spawnChunk.x- MagmaEntryHandler.SEARCH_OFFSET &&
                    mainChunkX <= spawnChunk.x+ MagmaEntryHandler.SEARCH_OFFSET)
                    && (mainChunkZ >= spawnChunk.z- MagmaEntryHandler.SEARCH_OFFSET &&
                    mainChunkZ <= spawnChunk.z+ MagmaEntryHandler.SEARCH_OFFSET)) {
                if (carver instanceof UnderwaterRavineCarver) {
                    MagmaEntryHandler.setViableRavineBlockAtIndex(x, z);
                } else {
                    MagmaEntryHandler.setViableCaveBlockAtIndex(x, z);
                }

            }
        }
    }
}
