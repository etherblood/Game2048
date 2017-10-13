package game2048;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Philipp
 */
public class LongStateTest {

    @Test
    public void testGetValue() {
        LongState state = new LongState();
        state.setState(0xfedcba9876543210L);
        for (int i = 0; i < 16; i++) {
            assertEquals(i, state.getValue(i));
        }
    }

    @Test
    public void testGetRow() {
        LongState state = new LongState();
        state.setState(0xfedcba9876543210L);
        assertEquals(0x3210, state.getRow(0));
        assertEquals(0x7654, state.getRow(1));
        assertEquals(0xba98, state.getRow(2));
        assertEquals(0xfedc, state.getRow(3));
    }

    @Test
    public void testGetColumn() {
        LongState state = new LongState();
        state.setState(0xfedcba9876543210L);
        assertEquals(0xc840, state.getColumn(0));
        assertEquals(0xd951, state.getColumn(1));
        assertEquals(0xea62, state.getColumn(2));
        assertEquals(0xfb73, state.getColumn(3));
    }

}