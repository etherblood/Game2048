package game2048;

import static game2048.DirectionMove.DOWN;
import static game2048.DirectionMove.RIGHT;
import static game2048.DirectionMove.UP;
import static game2048.DirectionMove.LEFT;
import game2048.mcts.Move;
import game2048.mymcts.AvailableMoves;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class AvailableDirectionMoves implements AvailableMoves<Move> {
    public final static List<Move> ALL = Arrays.asList(UP, RIGHT, DOWN, LEFT);
    public final static AvailableDirectionMoves INSTANCE = new AvailableDirectionMoves();

    @Override
    public boolean isMoveSelectedRandomly() {
        return false;
    }

    @Override
    public Move selectRandomMove(Random rng) {
        return ALL.get(rng.nextInt(4));
    }

    @Override
    public List<Move> toList() {
        return ALL;
    }

    @Override
    public int totalWeight() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int count() {
        return ALL.size();
    }

    @Override
    public int moveWeight(Move move) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
