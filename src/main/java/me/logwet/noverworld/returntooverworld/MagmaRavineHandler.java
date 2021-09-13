package me.logwet.noverworld.returntooverworld;


import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.util.BitMatrix;
import net.minecraft.util.math.ChunkPos;

import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.concurrent.atomic.AtomicBoolean;

public class MagmaRavineHandler extends FeatureHandler {
    public static final int SEARCH_OFFSET = 10;

    private static final AtomicBoolean active = new AtomicBoolean(false);

    private static BitMatrix viableBlocks;

    private static int viableBlockCount;

    private static int xBlockOffset;
    private static int zBlockOffset;

    private static final float[] mat = new float[]{
            1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f),
            1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f),
            1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f),
            1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f), 1f/(255f*16f)
    };
    private static final Kernel convolveKernel = new Kernel(4, 4, mat);
    private static final ConvolveOp convolver = new ConvolveOp(convolveKernel);



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

    public static synchronized void setViableBlockAtIndex(int x, int z) {
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
        /**
         * @author Al
         * Very wrinkly brain smart guy
         * Dunno how most of this works tbh
         *
         * 2D Range Sum Query
         */

        int[] params = viableBlocks.getEnclosingRectangle();
        int x = params[0];
        int y = params[1];
        int w = params[2];
        int h = params[3];

        int[][] sums = new int[h][w];
        sums[0][0] = viableBlocks.get(x, y) ? 1 : 0;

        for (int i = 1; i < h; i++) {
            int bit = viableBlocks.get(x, i + y) ? 1 : 0;
            sums[i][0] = sums[i - 1][0] + bit;
        }

        for (int i = 1; i < w; i++) {
            int bit = viableBlocks.get(x + i, y) ? 1 : 0;
            sums[0][i] = sums[0][i - 1] + bit;
        }

        for (int i = 1; i < h; i++) {
            for (int j = 1; j < w; j++) {

                int bit = viableBlocks.get(x + j, y + i) ? 1 : 0;
                if (viableBlocks.get(j + x, i + y)) {
                    System.out.println();
                }
                sums[i][j] = sums[i - 1][j] + sums[i][j - 1] - sums[i - 1][j - 1] + bit;
            }
        }

//        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(viableBlocks);
//        File outputFile = Paths.get("config/image1.png").toFile();
//        try {
//            ImageIO.write(bufferedImage, "png", outputFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}