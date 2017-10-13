package game2048.expectimax;

import java.util.Arrays;

/**
 *
 * @author Philipp
 */
public class TranspositionTable {
    private final long[] map;
    private final int mask;

    public TranspositionTable(int sizeBase) {
        this.map = new long[1 << sizeBase];
        mask = map.length - 2;
    }
    
    public void load(long hash, TranspositionEntry entry) {
        int index = (int)hash & mask;
        entry.setId(map[index]);
        entry.setData(map[index | 1]);
    }
    
    public void save(long hash, TranspositionEntry entry) {
        int index = (int) hash & mask;
        map[index] = entry.getId();
        map[index | 1] = entry.getData();
    }

    public void clear() {
        Arrays.fill(map, 0);
    }
    
}
