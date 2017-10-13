package game2048;

import game2048.mcts.Move;
import game2048.mcts.MovesInfo;
import game2048.mcts.Player;
import game2048.mcts.RandomSelector;
import game2048.mcts.SimulationState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class Mcts2048 implements SimulationState<Mcts2048, Move> {

    private final Random rng = new Random(17);
    private final List<Move> history = new ArrayList<>();
    private final LongState longState = new LongState();
    private final LongStateController controller = new LongStateController();

    @Override
    public void copyFrom(Mcts2048 state) {
        longState.setState(state.longState.getState());
    }

    @Override
    public void makeMove(Move move) {
        if (move instanceof DirectionMove) {
            controller.move(longState, ((DirectionMove) move).getDirection());
        } else {
            SetSquareValueMove ssvm = (SetSquareValueMove) move;
            longState.setValue(ssvm.getSquare(), ssvm.getValue());
        }
        history.add(move);
    }

    @Override
    public MovesInfo<Move> availableMoves() {
        MovesInfo<Move> info;
        if ((history.size() & 1) == 0) {
            info = new MovesInfo<>(new Player(0), Arrays.asList(DirectionMove.UP, DirectionMove.RIGHT, DirectionMove.DOWN, DirectionMove.LEFT));
        } else {
            List<Move> moves = new ArrayList<>();
            for (int square = 0; square < 16; square++) {
                int sqValue = longState.getValue(square);
                if (sqValue == 0) {
                    moves.add(new SetSquareValueMove(square, 1, 9));
                    moves.add(new SetSquareValueMove(square, 2, 1));
                }
            }
            info = new MovesInfo<>(null, moves);
        }
        return info;
    }

    @Override
    public Player currentPlayer() {
        if ((history.size() & 1) == 0) {
            return new Player(0);
        }
        return null;
    }

    @Override
    public float[] playout() {
        MovesInfo<Move> info;
        while(!(info = availableMoves()).getMoves().isEmpty()) {
            Move move = new RandomSelector().selectWeighted(rng, info.getMoves());
            makeMove(move);
        }
        return new float[]{((float)controller.score(longState) / (2 << 18))};
    }

    @Override
    public List<Move> history() {
        return history;
    }

}
