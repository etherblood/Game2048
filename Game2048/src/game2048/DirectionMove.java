package game2048;

import game2048.mcts.Move;

/**
 *
 * @author Philipp
 */
public class DirectionMove implements Move {

    public final static DirectionMove UP = new DirectionMove(LongStateController.UP);
    public final static DirectionMove RIGHT = new DirectionMove(LongStateController.RIGHT);
    public final static DirectionMove DOWN = new DirectionMove(LongStateController.DOWN);
    public final static DirectionMove LEFT = new DirectionMove(LongStateController.LEFT);
    
    private final int direction;

    private DirectionMove(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        switch(direction) {
            case LongStateController.UP:
                return "DirectionMove{UP}";
            case LongStateController.RIGHT:
                return "DirectionMove{RIGHT}";
            case LongStateController.DOWN:
                return "DirectionMove{DOWN}";
            case LongStateController.LEFT:
                return "DirectionMove{LEFT}";
        }
        throw new IllegalStateException(direction + " is not a valid direction");
    }
}
