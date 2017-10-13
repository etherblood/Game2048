package game2048.mymcts;

/**
 *
 * @author Philipp
 */
public interface ScoringFunction {
    double score(double parentVisits, double childVisits, double childWins);
}
