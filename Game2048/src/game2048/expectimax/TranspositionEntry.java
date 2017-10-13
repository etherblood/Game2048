package game2048.expectimax;

/**
 *
 * @author Philipp
 */
public class TranspositionEntry {
    private long id;
    private long data;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getData() {
        return data;
    }
    
    public void setDepthAndScore(int depth, int score) {
        data = ((long)depth << 32) | score;
    }

    public void setData(long data) {
        this.data = data;
    }
    
    public int getDepth() {
        return (int) (data >>> 32);
    }
    
    public int getScore() {
        return (int) data;
    }
}
