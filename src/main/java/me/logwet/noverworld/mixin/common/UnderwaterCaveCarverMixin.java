package me.logwet.noverworld.mixin.common;

import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.returntooverworld.MagmaRavineHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
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
        if (Noverworld.isFeatureHandlersActive() && MagmaRavineHandler.isActive() && carver instanceof UnderwaterRavineCarver) {
            ChunkPos spawnChunk = Noverworld.getWorldSpawnChunk();

            if ((mainChunkX >= spawnChunk.x-MagmaRavineHandler.SEARCH_OFFSET &&
                    mainChunkX <= spawnChunk.x+MagmaRavineHandler.SEARCH_OFFSET)
                    && (mainChunkZ >= spawnChunk.z-MagmaRavineHandler.SEARCH_OFFSET &&
                    mainChunkZ <= spawnChunk.z+MagmaRavineHandler.SEARCH_OFFSET)) {

                if (chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR, x, z) == 10) {
                    System.out.println("Underwater ravine  " + x +  " " + z);
                    MagmaRavineHandler.setViableBlockAtIndex(x, z);
                }
            }
        }
    }
}
