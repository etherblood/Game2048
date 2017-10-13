package game2048.mymcts;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public interface AvailableMoves<M> {
    boolean isMoveSelectedRandomly();
    M selectRandomMove(Random rng);
    List<M> toList();
    int totalWeight();
    int count();
    int moveWeight(M move);
}
