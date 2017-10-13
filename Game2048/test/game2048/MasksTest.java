package game2048;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Philipp
 */
public class MasksTest {


    @Test
    public void col0ToRow3() {
        long state = 0x123456789abcdefL;
        long expected = 0x37bf000000000000L;
        long actual = Masks.col0ToRow3(state);
        assertEquals(expected, actual);
    }
    @Test
    public void col0ToFlippedRow3() {
        long state = 0x123456789abcdefL;
        long expected = 0xfb73000000000000L;
        long actual = Masks.col0ToFlippedRow3(state);
        assertEquals(expected, actual);
    }

    @Test
    public void row0ToFlippedCol3() {
        long state = 0x123456789abcdefL;
        long expected = 0xf000e000d000c000L;
        assertEquals(expected, Masks.row0ToFlippedCol3(state));
    }

    @Test
    public void row0ToCol3() {
        long state = 0x123456789abcdefL;
        long expected = 0xc000d000e000f000L;
        assertEquals(expected, Masks.row0ToCol3(state));
    }

    @Test
    public void rotateCounterClockwise() {
        long state = 0x123456789abcdefL;
        long expected = 0xc840d951ea62fb73L;
        assertEquals(expected, Masks.rotateCounterClockwise(state));
    }

    @Test
    public void stresstestRotateCounterClockwise() {
        long seed = System.currentTimeMillis();
        System.out.println("stresstestRotateCounterClockwise with seed: " + seed);
        Random rng = new Random(seed);
        for (int i = 0; i < 1000; i++) {
            long state = rng.nextLong();
            long expected = state;
            for (int j = 0; j < 4; j++) {
                state = Masks.rotateCounterClockwise(state);
            }
            assertEquals(expected, state);
        }
    }

    @Test
    public void flipHorizontally() {
        long state = 0x123456789abcdefL;
        long expected = 0x32107654ba98fedcL;
        assertEquals(expected, Masks.flipHorizontally(state));
    }

    @Test
    public void flipVertically() {
        long state = 0x123456789abcdefL;
        long expected = 0xcdef89ab45670123L;
        assertEquals(expected, Masks.flipVertically(state));
    }

}