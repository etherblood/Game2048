package game2048.mcts;


public class VisitScore implements ScoringFunction {

    @Override
    public float score(float totalScore, float childTotal, float childScore) {
        return childTotal;
    }

}
