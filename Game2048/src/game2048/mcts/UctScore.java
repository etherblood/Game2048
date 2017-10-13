package game2048.mcts;

import java.util.Random;


public class UctScore implements ScoringFunction {
    private final static float EPSILON = 1e-6f;
    private final static float CONSTANT = (float) Math.sqrt(2);
    private final Random rng;

    public UctScore(Random rng) {
        this.rng = rng;
    }

    @Override
    public float score(float totalScore, float childTotal, float childScore) {
        childTotal += EPSILON;
        totalScore += 1;
        float exploitation = childScore / childTotal;
        float exploration = CONSTANT * (float) (Math.sqrt(Math.log(totalScore) / childTotal));
        float uctValue = exploitation + exploration;
        uctValue += EPSILON * rng.nextFloat();
        return uctValue;
    }

}
