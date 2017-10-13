package game2048.expectimax;

import java.util.Random;

/**
 *
 * @author Philipp
 */
public class Zobrist {
    private final long[] hashes = new long[1 << 16];
    private final int mask = hashes.length - 1;

    public Zobrist(Random rng) {
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = rng.nextLong();
        }
    }
    
    public long hash(long id) {
        long hash = hashes[(int)id & mask];
        id >>>= 8;
        hash ^= hashes[(int)id & mask];
        id >>>= 8;
        hash ^= hashes[(int)id & mask];
        id >>>= 8;
        hash ^= hashes[(int)id & mask];
        return hash;
    }
}
