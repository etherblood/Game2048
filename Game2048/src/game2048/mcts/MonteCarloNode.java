package game2048.mcts;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Philipp
 */
public class MonteCarloNode<M extends Move> {
    private MovesInfo<M> movesInfo;
    private Map<M, MonteCarloNode<M>> childs;
//    private Map<M, MonteCarloNode> childs;
    private final float[] playerScores;

    public MonteCarloNode(int numPlayers) {
        playerScores = new float[numPlayers];
        Arrays.fill(playerScores, 0);
    }
    
    public boolean isInitialized() {
        return movesInfo != null;
    }
    
    public void init(MovesInfo<M> moves) {
        this.movesInfo = moves;
        this.childs = new HashMap<>();
        moves.getMoves().forEach((move) -> {
            childs.computeIfAbsent(move, m -> new MonteCarloNode<>(playerScores.length));
        });
    }
    
//    public MonteCarloNode getOrCreateChild(M move) {
//        return childs.computeIfAbsent(move, m -> new MonteCarloNode(playerScores.length));
//    }
    
//    public boolean hasChilds() {
//        return childs != null && !isLeaf();
//    }
    
    public MonteCarloNode<M> getChild(M move) {
        return childs.get(move);
    }
    
    public Collection<MonteCarloNode<M>> getChilds() {
        return childs.values();
    }
    
    public boolean isLeaf() {
        return childs.isEmpty();
    }
    
    public void increaseScores(float[] scores) {
        for (int i = 0; i < playerScores.length; i++) {
            playerScores[i] += scores[i];
        }
    }

    public float totalScore() {
        float sum = 0;
        for (float playerScore : playerScores) {
            sum += playerScore;
        }
        return sum;
    }

    public float playerScore(Player currentPlayer) {
        return playerScores[currentPlayer.getId()];
    }

    public MovesInfo<M> getMovesInfo() {
        return movesInfo;
    }
}
