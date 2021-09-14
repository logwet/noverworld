package me.logwet.noverworld.returntooverworld;


import me.logwet.noverworld.Noverworld;
import me.logwet.noverworld.util.BitMatrix;
import net.minecraft.util.math.ChunkPos;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MagmaRavineHandler extends FeatureHandler {
    public static final int SEARCH_OFFSET = 11;
    private static final int MATRIX_DIM = (SEARCH_OFFSET * 2 + 1) * 16;
    public static final int SEARCH_BOX_SIZE = 4;

    private static final AtomicBoolean active = new AtomicBoolean(false);

    /**
     * BitMatrix from zxing is used because it is memory efficient. The entire 336x336 matrix is under 15 kilobytes.
     */
    private static final BitMatrix viableBlocks = new BitMatrix(MATRIX_DIM);

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
        return getViableBlockCount() >= 16;
    }

    public static void reset() {
        viableBlocks.clear();
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

    /**
     * @author Al
     * Very wrinkly brain smart guy
     * Dunno how most of this works tbh
     *
     * 2D Range Sum Query (immutable) https://leetcode.com/problems/range-sum-query-2d-immutable/
     * Amortizing the range sum
     * then query it for a 4x4 region for portal placement
     *
     * This algorithm is pretty fast. Takes roughly 2 ms per 500 blocks of ravine detected.
     *
     * @return array containing x and z of where to place
     */
    public static int[] searchForSuitableArea() {
        int[] params = Objects.requireNonNull(viableBlocks.getEnclosingRectangle());
        int x = params[0];
        int z = params[1];
        int w = params[2];
        int h = params[3];

        /**
         * sumMatrix will at most be 446 kilobytes
         */
        final int[][] sumMatrix = new int[h][w];

        sumMatrix[0][0] = viableBlocks.get(x, z) ? 1 : 0;

        int bit;

        int i, j;

        /**
         * Amortize the range sum matrix
         */
        for (i = 1; i < h; i++) {
            bit = viableBlocks.get(x, z + i) ? 1 : 0;
            sumMatrix[i][0] = sumMatrix[i - 1][0] + bit;
        }

        for (i = 1; i < w; i++) {
            bit = viableBlocks.get(x + i, z) ? 1 : 0;
            sumMatrix[0][i] = sumMatrix[0][i - 1] + bit;
        }

        for (i = 1; i < h; i++) {
            for (j = 1; j < w; j++) {
                bit = viableBlocks.get(x + j, z + i) ? 1 : 0;
                sumMatrix[i][j] = sumMatrix[i - 1][j] + sumMatrix[i][j - 1] - sumMatrix[i - 1][j - 1] + bit;
            }
        }

        int closestDistance = Integer.MAX_VALUE;
        int bestX = -1, bestZ = -1, distanceFromSpawn;

        int middlePos = SEARCH_OFFSET * 16 + 8;

        int summedSearchBox, tX, tZ;

        /**
         * Iterate through the generated 2d range sum to find a 4x4 area
         */
        for (i = SEARCH_BOX_SIZE; i < h; i++) {
            for (j = SEARCH_BOX_SIZE; j < w; j++) {
                summedSearchBox = sumMatrix[i][j] - sumMatrix[i - SEARCH_BOX_SIZE][j] -
                        sumMatrix[i][j - SEARCH_BOX_SIZE] + sumMatrix[i - SEARCH_BOX_SIZE][j - SEARCH_BOX_SIZE];
                if (summedSearchBox == SEARCH_BOX_SIZE * SEARCH_BOX_SIZE) {
                    tX = x + j;
                    tZ = z + i;

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
            return new int[]{bestX-xBlockOffset-SEARCH_BOX_SIZE, bestZ-zBlockOffset-SEARCH_BOX_SIZE};
        }
        return null;
    }

}