package game2048.expectimax.eval;

import game2048.SimpleLongState;

/**
 *
 * @author Philipp
 */
public interface Evaluation {
    float evaluate(SimpleLongState state);
}
