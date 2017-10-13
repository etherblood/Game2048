package game2048.mcts;

import java.util.List;

/**
 *
 * @author Philipp
 */
public class MovesInfo<M extends Move> {
    private final Player actor;
    private final List<M> moves;

    public MovesInfo(Player actor, List<M> moves) {
        this.actor = actor;
        this.moves = moves;
    }
    
    public boolean isRandom() {
        return actor == null;
    }
    
    public Player getActor() {
        return actor;
    }

    public List<M> getMoves() {
        return moves;
    }
}
