package game2048.mcts;

import java.util.List;

/**
 *
 * @author Philipp
 */
public interface SimulationState<S extends SimulationState, M extends Move> {
    void copyFrom(S state);
    void makeMove(M move);
    MovesInfo<M> availableMoves();
    Player currentPlayer();
    float[] playout();
    List<M> history();
}
