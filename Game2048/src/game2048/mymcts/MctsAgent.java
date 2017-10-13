package game2048.mymcts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Philipp
 */
public class MctsAgent<S, M, I> {
    private final static boolean TRANSPOSITIONS_ENABLED = true;
    
    private final Random rng;
    private final MctsStateController<S, M, I> controller;
    private MctsNode<M> root = new MctsNode<>();
    private final S simulationState;
    private final UctScore uct = new UctScore();
    private final VisitScore visit = new VisitScore();
    private final Map<I, MctsNode<M>> ttMap = new HashMap<>();
    
    private double maxScore, minScore;

    public MctsAgent(Random rng, MctsStateController<S, M, I> controller, S simulationState) {
        this.rng = rng;
        this.controller = controller;
        this.simulationState = simulationState;
    }
    
    public void iteration(S state) {
        controller.copy(state, simulationState);
        minScore = controller.score(state);
        maxScore = 10000 + minScore;
//        currentState.copyFrom(state);
        List<M> path = new ArrayList<>();
        MctsNode<M> itStartNode = root;
        MctsNode<M> currentNode = itStartNode;
        while (currentNode.isInitialized() && !controller.isGameOver(simulationState)) {
            M move = selectChild(currentNode, uct);
            MctsNode<M> child = currentNode.getChild(move);
            
            if (child != null && currentNode.getChildVisits(move) < child.getTotalVisits()) {
                //take score from transposition and exit early
                assert TRANSPOSITIONS_ENABLED;
                currentNode.increaseChildVisits(move, 1);
                double result = child.getAverageWins();
                propagateResult(root, path, result);
                return;
            }
            
            path.add(move);
            currentNode = gotoChild(currentNode, move);
        }
        tryExpand(currentNode, path);
        double result = controller.playout(simulationState, rng);
        propagateResult(itStartNode, path, result);
    }
    
    private void propagateResult(MctsNode<M> startNode, List<M> path, double result) {
        MctsNode<M> currentNode = startNode;
        for (M move : path) {
            currentNode.addWins(result);
            currentNode.addVisits(1);
            currentNode.increaseChildVisits(move, 1);
            currentNode = currentNode.getChild(move);
        }
        currentNode.addWins(result);
        currentNode.addVisits(1);
    }

    private MctsNode<M> gotoChild(MctsNode<M> currentNode, M move) {
        controller.applyMove(simulationState, move);
        MctsNode<M> child = currentNode.getChild(move);
        if(child == null) {
            if(TRANSPOSITIONS_ENABLED) {
                I id = controller.getId(simulationState);
                child = ttMap.get(id);
            }
            if(child == null) {
                child = new MctsNode<>();
            }
            currentNode.setChild(move, child);
        }
        return child;
    }

    long tts = 0;
    long total = 0;
    private void tryExpand(MctsNode<M> currentNode, List<M> path) {
        if(!currentNode.isInitialized()) {
            currentNode.init(new HashMap<>(), new HashMap<>());
            if (TRANSPOSITIONS_ENABLED) {
                MctsNode<M> old = ttMap.put(controller.getId(simulationState), currentNode);
                if (old != null) {
                    tts++;
                }
                total++;
            }
            if(!controller.isGameOver(simulationState)) {
                AvailableMoves<M> moves = controller.availableMoves(simulationState);
                M move = moves.selectRandomMove(rng);
                path.add(move);
                gotoChild(currentNode, move);
            }
        }
    }

    private M selectChild(MctsNode<M> currentNode, ScoringFunction score) {
        AvailableMoves<M> availableMoves = controller.availableMoves(simulationState);
        if(availableMoves.isMoveSelectedRandomly()) {
            return availableMoves.selectRandomMove(rng);
        }
        
//        List<MctsNode<M>> nodes = moves.stream().map(currentNode::getChild).collect(Collectors.toList());
//        assert nodes.contains(currentNode);
//        for (MctsNode<M> node : nodes) {
//            List<M> nodeMoves = node.getMovesList().getMoves();
//            assert nodeMoves.containsAll(moves);
//            assert moves.containsAll(nodeMoves);
//        }
//        
//        Map<Class<?>, List<M>> groups = moves.stream().collect(Collectors.groupingBy(Object::getClass));
//        
//        List<M> bestGroup = selectGroup(groups.values(), nodes, info.getActor(), score);
        
        return selectMove(currentNode, availableMoves, score);
    }

//    private List<M> selectGroup(Collection<List<M>> groups, List<MctsNode<M>> nodes, Player player, ScoringFunction score) {
//        double childsTotal = (double) nodes.stream()
//                .flatMap(n -> n.getChilds().stream())
//                .mapToDouble(MctsNode::totalScore)
//                .sum();
//        
//        List<M> bestGroup = null;
//        double bestGroupScore = Float.NEGATIVE_INFINITY;
//        for (List<M> group : groups) {
//            double visitScore = 0;
//            double playerScore = 0;
//
//            for (M move : group) {
//                for (MctsNode<M> node : nodes) {
//                    MctsNode child = node.getChild(move);
//                    visitScore += child.totalScore();
//                    playerScore += child.playerScore(player);
//                }
//            }
//
//            double groupScore = score.score(childsTotal, visitScore, playerScore);
//            if (groupScore > bestGroupScore) {
//                bestGroupScore = groupScore;
//                bestGroup = group;
//            }
//        }
//        assert bestGroup != null;
//        return bestGroup;
//    }

    private M selectMove(MctsNode<M> node, AvailableMoves<M> availableMoves, ScoringFunction score) {
        List<M> moveList = availableMoves.toList();
        double childVisits = 0;
        for (M move : moveList) {
            childVisits += node.getChildVisits(move);
        }
        
        List<M> bestMoves = new ArrayList<>();
        double bestScore = Float.NEGATIVE_INFINITY;
        for (M move : moveList) {
            double visits = node.getChildVisits(move);
            MctsNode<M> child = node.getChild(move);
            double childWins;
            if(child == null) {
                childWins = minScore;
            } else {
                childWins = child.getAverageWins() * visits;
            }
            
            double childScore = score.score(childVisits, visits, (childWins - minScore) / (maxScore - minScore));
            if (childScore >= bestScore) {
                if(childScore != bestScore) {
                    bestMoves.clear();
                }
                bestMoves.add(move);
                bestScore = childScore;
            }
        }
        return RandomSelector.select(rng, bestMoves);
    }

    public M bestChild(S state) {
        controller.copy(state, simulationState);
        return selectChild(root, visit);
    }
    
    public double simulationStrength(S state) {
        return root.getTotalVisits();
    }
    
    public double simulationConfidence(S state, M move) {
        MctsNode<M> child = root.getChild(move);
        if(child == null) {
            return 0;
        }
        return child.getTotalVisits() / root.getTotalVisits();
    }
    
    public void onMoveApplied(S state, M move) {
        if(root.isInitialized()) {
            root = root.getChild(move);
            if(root == null) {
                root = new MctsNode<>();
            }
        }
    }
    
    public void removeUnreachableTranspositions() {
        Set<MctsNode<M>> reachableNodes = new HashSet<>();
        visit(root, reachableNodes);
        int removedCount = 0;
        Iterator<Map.Entry<I, MctsNode<M>>> iterator = ttMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<I, MctsNode<M>> next = iterator.next();
            if(!reachableNodes.contains(next.getValue())) {
                iterator.remove();
                removedCount++;
            }
        }
        if(removedCount > 0) {
            System.out.println("removed " + removedCount + " unreachable transpositions, " + ttMap.size() + " remaining.");
        }
    }
    
    private void visit(MctsNode<M> node, Set<MctsNode<M>> reachableNodes) {
        if(node.isInitialized() && reachableNodes.add(node)) {
            for (MctsNode<M> child : node.getChilds()) {
                visit(child, reachableNodes);
            }
        }
    }
    
    public void clearTree() {
        root = new MctsNode<>();
        removeUnreachableTranspositions();
    }
}
