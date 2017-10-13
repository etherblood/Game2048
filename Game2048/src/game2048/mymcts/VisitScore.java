package game2048.mymcts;


public class VisitScore implements ScoringFunction {

    @Override
    public double score(double totalScore, double childTotal, double childScore) {
        return childTotal;
    }

}
