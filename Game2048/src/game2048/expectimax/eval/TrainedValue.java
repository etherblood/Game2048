package game2048.expectimax.eval;

/**
 *
 * @author Philipp
 */
public class TrainedValue {
    private float avg;
    
    public float get() {
        return avg;
    }
    
    public void set(float value) {
        avg = value;
    }
    
    public void inc(float value) {
        avg += value;
    }
    
}
