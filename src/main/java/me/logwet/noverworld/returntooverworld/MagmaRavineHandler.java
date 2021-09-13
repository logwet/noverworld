package me.logwet.noverworld.returntooverworld;


import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.util.BitMatrix;
import me.logwet.noverworld.util.MatrixToImageWriter;
import net.minecraft.util.math.ChunkPos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class MagmaRavineHandler extends FeatureHandler {
    public static final int SEARCH_OFFSET = 10;

    private static final AtomicBoolean active = new AtomicBoolean(false);

    private static BitMatrix viableBlocks;

    private static int viableBlockCount;

    private static int xBlockOffset;
    private static int zBlockOffset;

    public static boolean isActive() {
        return active.get();
    }

    public static void setActive(boolean active) {
        MagmaRavineHandler.active.set(active);
    }

    public static BitMatrix getViableBlocks() {
        return viableBlocks;
    }

    public static int getViableBlockCount() {
        return viableBlockCount;
    }

    public static boolean ifFoundViableBlock() {
        return getViableBlockCount() > 0;
    }

    public static void reset() {
        int dim = (SEARCH_OFFSET * 2 + 1) * 16;
        viableBlocks = new BitMatrix(dim);
        viableBlockCount = 0;
        genOffsets();
        setActive(true);
    }

    private static void genOffsets() {
        ChunkPos spawnChunk = Noverworld.getWorldSpawnChunk();
        ChunkPos offsetChunkPos = new ChunkPos(spawnChunk.x-SEARCH_OFFSET, spawnChunk.z-SEARCH_OFFSET);
        xBlockOffset = -offsetChunkPos.getStartX();
        zBlockOffset = -offsetChunkPos.getStartZ();
    }

    public static void setViableBlockAtIndex(int x, int z) {
        viableBlocks.set(x + xBlockOffset, z + zBlockOffset);
        viableBlockCount++;
    }

    public static boolean getViableBlockAtIndex(int x, int z) {
        return viableBlocks.get(x + xBlockOffset, z + zBlockOffset);
    }

    public static void flipViableBlockAtIndex(int x, int z) {
        viableBlocks.flip(x + xBlockOffset, z + zBlockOffset);
        viableBlockCount--;
    }

    public static void convolveForSuitableArea() {
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(viableBlocks);
        File outputFile = Paths.get("config/image.png").toFile();
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}