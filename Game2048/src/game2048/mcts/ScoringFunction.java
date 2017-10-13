package game2048.mcts;

/**
 *
 * @author Philipp
 */
public interface ScoringFunction {
    float score(float totalScore, float childTotal, float childScore);
}
