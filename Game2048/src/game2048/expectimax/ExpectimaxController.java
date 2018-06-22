package game2048.expectimax;

import java.util.List;

/**
 *
 * @author Philipp
 */
public interface ExpectimaxController<M> {

    void make(M move);
    void unmake(M move);
    
    boolean isNextMoveRandom();
    List<M> availableMoves();
    Iterable<M> sampleMoves(int num);
    int moveWeight(M move);
    
    float eval();
    long id();

    boolean isGameOver();
}
