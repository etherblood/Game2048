package game2048;

import game2048.mcts.Move;

/**
 *
 * @author Philipp
 */
public class XorMove implements Move {

    private final long move;
    private final int weight;

    public XorMove(long move) {
        this(move, 0);
    }

    public XorMove(long move, int weight) {
        this.move = move;
        this.weight = weight;
    }

    public long getMove() {
        return move;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isRandom() {
        return weight != 0;
    }
}
