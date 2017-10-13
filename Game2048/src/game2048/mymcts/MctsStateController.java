package game2048.mymcts;

import java.util.Random;

/**
 *
 * @author Philipp
 */
public interface MctsStateController<S, M, I> {
    void copy(S from, S to);
    void applyMove(S state, M move);
    float score(S state);
    boolean isGameOver(S state);
    AvailableMoves<M> availableMoves(S state);
    I getId(S state);
    default float playout(S state, Random rng) {
        while(!isGameOver(state)) {
            AvailableMoves<M> info = availableMoves(state);
            M move = info.selectRandomMove(rng);
            applyMove(state, move);
        }
        return score(state);
    }
}
