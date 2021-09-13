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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MagmaRavineHandler extends FeatureHandler {
    public static final int SEARCH_OFFSET = 10;
    private static final int SEARCH_BOX_SIZE = 4;

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

    public static int[] searchForSuitableArea() throws Exception {
        /**
         * @author Al
         * Very wrinkly brain smart guy
         * Dunno how most of this works tbh
         *
         * 2D Range Sum Query
         * Amortizing the range sum
         * then query it for a 4x4 region for portal placement
         */

        int[] params = Objects.requireNonNull(viableBlocks.getEnclosingRectangle());
        int x = params[0];
        int y = params[1];
        int w = params[2];
        int h = params[3];

        int[][] sumMatrix = new int[h][w];
        sumMatrix[0][0] = viableBlocks.get(x, y) ? 1 : 0;

        int bit;

        for (int i = 1; i < h; i++) {
            bit = viableBlocks.get(x, i + y) ? 1 : 0;
            sumMatrix[i][0] = sumMatrix[i - 1][0] + bit;
        }

        for (int i = 1; i < w; i++) {
            bit = viableBlocks.get(x + i, y) ? 1 : 0;
            sumMatrix[0][i] = sumMatrix[0][i - 1] + bit;
        }

        for (int i = 1; i < h; i++) {
            for (int j = 1; j < w; j++) {
                bit = viableBlocks.get(x + j, y + i) ? 1 : 0;
                sumMatrix[i][j] = sumMatrix[i - 1][j] + sumMatrix[i][j - 1] - sumMatrix[i - 1][j - 1] + bit;
            }
        }

        int closestDistance = Integer.MAX_VALUE;
        int bestX = -1, bestZ = -1, distanceFromSpawn;

        int middlePos = SEARCH_OFFSET * 16 + 8;

        for (int i = SEARCH_BOX_SIZE; i < h; i++) {
            for (int j = SEARCH_BOX_SIZE; j < w; j++) {
                int sum4x4 = sumMatrix[i][j] - sumMatrix[i - SEARCH_BOX_SIZE][j] - sumMatrix[i][j - SEARCH_BOX_SIZE] +
                        sumMatrix[i - SEARCH_BOX_SIZE][j - SEARCH_BOX_SIZE];
                if (sum4x4 == SEARCH_BOX_SIZE * SEARCH_BOX_SIZE) {
                    int tX = x + j;
                    int tZ = y + i;

                    distanceFromSpawn = (tX - middlePos)*(tX - middlePos) + (tZ - middlePos)*(tZ - middlePos);

                    if (distanceFromSpawn < closestDistance) {
                        closestDistance = distanceFromSpawn;
                        bestX = tX;
                        bestZ = tZ;
                    }
                }
            }
        }

        if (bestX >=0 && bestZ >= 0) {
            return new int[]{bestX-xBlockOffset, bestZ-zBlockOffset};
        }
        throw new Exception("Unable to find location for RTO portal");
    }

}