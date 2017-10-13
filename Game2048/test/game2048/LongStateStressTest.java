package game2048;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Philipp
 */
public class LongStateStressTest {

    @Test
    public void testColumnGet() {
        long millis = System.currentTimeMillis();
        System.out.println("testColumnGet with seed: " + millis);
        Random rng = new Random(millis);
        LongState state = new LongState();
        for (int i = 0; i < 1000; i++) {
            long initialState = rng.nextLong();
            state.setState(initialState);
            int columnValue = rng.nextInt(1 << 16);
            int x = rng.nextInt(4);
            state.setValue(state.square(x, 0), columnValue & 0xf);
            state.setValue(state.square(x, 1), (columnValue >>> 4) & 0xf);
            state.setValue(state.square(x, 2), (columnValue >>> 8) & 0xf);
            state.setValue(state.square(x, 3), (columnValue >>> 12) & 0xf);
            assertEquals(columnValue, state.getColumn(x));
//            state.setColumn(x, columnValue);
//            assertEquals("initialState: " + Long.toHexString(initialState) + ", state: " + Long.toHexString(state.getState()) + ", x: " + x, columnValue, state.getColumn(x));
        }
    }
    @Test
    public void testColumnSet() {
        long millis = System.currentTimeMillis();
        System.out.println("testColumnSet with seed: " + millis);
        Random rng = new Random(millis);
        LongState state = new LongState();
        for (int i = 0; i < 1000; i++) {
            long initialState = rng.nextLong();
            state.setState(initialState);
            int columnValue = rng.nextInt(1 << 16);
            int x = rng.nextInt(4);
            state.setColumn(x, columnValue);
            assertEquals(columnValue & 0xf, state.getValue(state.square(x, 0)));
            assertEquals((columnValue >>> 4) & 0xf, state.getValue(state.square(x, 1)));
            assertEquals((columnValue >>> 8) & 0xf, state.getValue(state.square(x, 2)));
            assertEquals((columnValue >>> 12) & 0xf, state.getValue(state.square(x, 3)));
//            assertEquals("initialState: " + Long.toHexString(initialState) + ", state: " + Long.toHexString(state.getState()) + ", x: " + x, columnValue, state.getColumn(x));
        }
    }

    @Test
    public void testRows() {
        long millis = System.currentTimeMillis();
        System.out.println("testRows with seed: " + millis);
        Random rng = new Random(millis);
        LongState state = new LongState();
        for (int i = 0; i < 1000; i++) {
            long initialState = rng.nextLong();
            state.setState(initialState);
            int rowValue = rng.nextInt(1 << 16);
            int y = rng.nextInt(4);
            state.setRow(y, rowValue);
            assertEquals("initialState: " + Long.toHexString(initialState) + ", state: " + Long.toHexString(state.getState()) + ", y: " + y, rowValue, state.getRow(y));
        }
    }

}
