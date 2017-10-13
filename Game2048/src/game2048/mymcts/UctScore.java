package game2048.mymcts;

public class UctScore implements ScoringFunction {

    private final static double CONSTANT = Math.sqrt(2);

    @Override
    public double score(double parentVisits, double childVisits, double childWins) {
        assert childVisits <= parentVisits;
//        assert 0 <= childWins;
//        assert childWins <= childVisits : childWins + " / " + childVisits;
        if(childVisits == 0) {
            return Float.POSITIVE_INFINITY;
        }
        double exploitation = childWins / childVisits;
        double exploration = CONSTANT * Math.sqrt(Math.log(Math.max(parentVisits, 1)) / childVisits);
        double uctValue = exploitation + exploration;
        return uctValue;
    }

}
