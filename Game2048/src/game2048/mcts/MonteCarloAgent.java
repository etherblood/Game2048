package game2048.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author Philipp
 */
public class MonteCarloAgent<S extends SimulationState<S, M>, M extends Move> {
    private final Random rng = new Random(0);
    private final S currentState;
    private final UctScore uct = new UctScore(rng);
    private final VisitScore visit = new VisitScore();
    private final MonteCarloNode<M> root;

    public MonteCarloAgent(S currentState, MonteCarloNode<M> root) {
        this.currentState = currentState;
        this.root = root;
    }

    public void iteration(S state) {
        currentState.copyFrom(state);
        List<M> path = new ArrayList<>();
        MonteCarloNode<M> itStartNode = currentNode();
        MonteCarloNode<M> currentNode = select(itStartNode, path);
        tryExpand(currentNode, path);
        float[] result = currentState.playout();
        propagateResult(itStartNode, path, result);
    }
    
    private void propagateResult(MonteCarloNode<M> startNode, List<M> path, float[] result) {
        MonteCarloNode<M> currentNode = startNode;
        for (M move : path) {
            currentNode.increaseScores(result);
            currentNode = currentNode.getChild(move);
        }
        currentNode.increaseScores(result);
    }

    private MonteCarloNode<M> select(MonteCarloNode<M> startNode, List<M> path) {
        MonteCarloNode<M> currentNode = startNode;
        while (currentNode.isInitialized() && !currentNode.isLeaf()) {
            M move = selectChild(currentNode, uct);
            path.add(move);
            currentNode = gotoChild(currentNode, move);
        }
        return currentNode;
    }

    private MonteCarloNode<M> gotoChild(MonteCarloNode<M> currentNode, M move) {
        currentState.makeMove(move);
        return currentNode.getChild(move);
    }

    private void tryExpand(MonteCarloNode currentNode, List<M> path) {
        if(!currentNode.isInitialized()) {
            MovesInfo<M> moves = currentState.availableMoves();
            currentNode.init(moves);
            if(!currentNode.isLeaf()) {
                M childIndex = moves.getMoves().get(rng.nextInt(moves.getMoves().size()));
                path.add(childIndex);
                gotoChild(currentNode, childIndex);
            }
        }
    }

    private M selectChild(MonteCarloNode<M> currentNode, ScoringFunction score) {
        assert currentNode == currentNode();
        MovesInfo<M> info = currentNode.getMovesInfo();
        List<M> moves = info.getMoves();
        if(info.isRandom()) {
            return new RandomSelector().selectWeighted(rng, moves);//moves.get(rng.nextInt(moves.size()));
        }
        assert currentState.currentPlayer().getId() == info.getActor().getId();
        
        List<MonteCarloNode<M>> nodes = moves.stream().map(currentNode::getChild).collect(Collectors.toList());
        assert nodes.contains(currentNode);
        for (MonteCarloNode<M> node : nodes) {
            List<M> nodeMoves = node.getMovesInfo().getMoves();
            assert nodeMoves.containsAll(moves);
            assert moves.containsAll(nodeMoves);
        }
        
        Map<Class<?>, List<M>> groups = moves.stream().collect(Collectors.groupingBy(Object::getClass));
        
        List<M> bestGroup = selectGroup(groups.values(), nodes, info.getActor(), score);
        
        return selectMove(bestGroup, nodes, info.getActor(), score);
    }

    private List<M> selectGroup(Collection<List<M>> groups, List<MonteCarloNode<M>> nodes, Player player, ScoringFunction score) {
        float childsTotal = (float) nodes.stream()
                .flatMap(n -> n.getChilds().stream())
                .mapToDouble(MonteCarloNode::totalScore)
                .sum();
        
        List<M> bestGroup = null;
        float bestGroupScore = Float.NEGATIVE_INFINITY;
        for (List<M> group : groups) {
            float visitScore = 0;
            float playerScore = 0;

            for (M move : group) {
                for (MonteCarloNode<M> node : nodes) {
                    MonteCarloNode child = node.getChild(move);
                    visitScore += child.totalScore();
                    playerScore += child.playerScore(player);
                }
            }

            float groupScore = score.score(childsTotal, visitScore, playerScore);
            if (groupScore > bestGroupScore) {
                bestGroupScore = groupScore;
                bestGroup = group;
            }
        }
        assert bestGroup != null;
        return bestGroup;
    }

    private M selectMove(List<M> group, List<MonteCarloNode<M>> nodes, Player player, ScoringFunction score) {
        float childsTotal = (float) nodes.stream()
                .flatMap(n -> group.stream().map(n::getChild))
                .mapToDouble(MonteCarloNode::totalScore)
                .sum();
        
        M best = null;
        float bestScore = Float.NEGATIVE_INFINITY;
        for (M move : group) {
            float visitScore = 0;
            float playerScore = 0;
            for (MonteCarloNode<M> node : nodes) {
                MonteCarloNode child = node.getChild(move);
                visitScore += child.totalScore();
                playerScore += child.playerScore(player);
            }
            
            float childScore = score.score(childsTotal, visitScore, playerScore);
            if (childScore > bestScore) {
                bestScore = childScore;
                best = move;
            }
        }
        assert best != null;
        return best;
    }

    public Move bestChild(S state) {
        currentState.copyFrom(state);
        return selectChild(currentNode(), visit);
    }
    
    public float simulationStrength(S state) {
        currentState.copyFrom(state);
        return currentNode().totalScore();
    }
    
    public float simulationConfidence(S state, M move) {
        currentState.copyFrom(state);
        MonteCarloNode<M> currentNode = currentNode();
        MonteCarloNode<M> c = currentNode.getChild(move);
        return c.totalScore() / currentNode.totalScore();
    }
    
    private MonteCarloNode<M> currentNode() {
        MonteCarloNode<M> currentNode = root;
        for (M m : currentState.history()) {
            currentNode = currentNode.getChild(m);
        }
        return currentNode;
    }
}
