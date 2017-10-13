package game2048;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Philipp
 */
public class LongStateControllerTest {

    public LongStateControllerTest() {
    }

    @Test
    public void moveLeft() {
        LongStateController controller = new LongStateController();
        assertEquals(0x2, controller.moveDown(0x11));
        assertEquals(0x22, controller.moveDown(0x1111));
        assertEquals(0x1234, controller.moveDown(0x1234));
        assertEquals(0x212, controller.moveDown(0x2120));
        assertEquals(0x212, controller.moveDown(0x2102));
        assertEquals(0x212, controller.moveDown(0x2012));
    }
    
    @Test
    public void moveStateLeft() {
        LongState state = new LongState();
        state.setState(0x22000100000000L);
        LongStateController controller = new LongStateController();
        controller.move(state, LongStateController.LEFT);
        assertEquals(0x3000100000000L, state.getState());
    }
    
    @Test(expected = LongStateOverflowException.class)
    public void overflow() {
        LongStateController controller = new LongStateController();
        assertEquals(LongStateController.OVERFLOW, controller.moveDown(0xf0f));
    }

}