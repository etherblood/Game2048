package game2048.expectimax.eval.treestrap;

import game2048.MyMctsController;
import game2048.SimpleLongState;
import game2048.expectimax.eval.SimpleTrainedEval;
import game2048.expectimax.eval.TrainedValue;
import game2048.mcts.Move;
import game2048.mymcts.AvailableMoves;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class TreeStrap {

    MyMctsController controller = new MyMctsController();

    public static void main(String... args) throws IOException {
        TreeStrap treeStrap = new TreeStrap();
        Random rng = new Random(3);
        SimpleTrainedEval eval = new SimpleTrainedEval();
//        for (int i = 0; i < 17; i++) {
//            for (int j = 0; j < 2; j++) {
//                for (int k = 0; k < eval.all_scores[i][j].length; k++) {
//                    eval.all_scores[i][j][k].set(rng.nextFloat() * 4 + 1);
//                }
//            }
//            eval.all_playerMoves[i].set(rng.nextFloat() * 4 + 1);
//        }

        for (int i = 0; i < 1000000; i++) {
            treeStrap.iteration(rng, eval);
            System.out.println(i);
            System.out.println(String.format("%.2f", eval.evaluate(new SimpleLongState())));
        }
    }

//heuristic function Hθ(s) to evaluate non-terminal states at depth D
    //θ is a parameter vector specifying the weight of each feature in the linear combination
    public void iteration(Random rng, SimpleTrainedEval eval) {
        float stepSize = 0.000001f;
        int startDepth = 4;
        ParameterVector<TrainedValue> parameterVector = new ParameterVector<>();
        SimpleLongState state = new SimpleLongState();
        while (!controller.isGameOver(state)) {
            ParameterVector<TrainedValue> delta0 = new ParameterVector<>();
            Map<SimpleLongState, Float> searchTree = expectimax(state, eval, startDepth);
            for (SimpleLongState childState : searchTree.keySet()) {
                float scoreDelta = searchTree.get(childState) - eval.evaluate(childState);
                ParameterVector<TrainedValue> childFeatureVector = eval.featureVecorOf(childState);
                childFeatureVector.multLocal(stepSize * scoreDelta);
                delta0.addLocal(childFeatureVector);
            }
            parameterVector.addLocal(delta0);

            if (state.isPlayerTurn()) {
                float bestScore = Float.NEGATIVE_INFINITY;
                Move best = null;
                for (Move move : controller.availableMoves(state).toList()) {
                    SimpleLongState tmpState = new SimpleLongState(state);
                    controller.applyMove(tmpState, move);
                    float moveScore = controller.isGameOver(tmpState) ? controller.score(tmpState) : searchTree.get(tmpState);//eval.eval(tmpState);
                    if (moveScore > bestScore) {
                        best = move;
                        bestScore = moveScore;
                    }
                }
                controller.applyMove(state, best);
            } else {
                controller.applyMove(state, controller.availableMoves(state).selectRandomMove(rng));
            }
        }
        for (Map.Entry<TrainedValue, Float> entry : parameterVector.entrySet()) {
            entry.getKey().inc(entry.getValue());
        }
    }

    private Map<SimpleLongState, Float> expectimax(SimpleLongState state, SimpleTrainedEval eval, int depth) {
        Map<SimpleLongState, Float> tree = new HashMap<>();
        tree.put(new SimpleLongState(state), expectimax(tree, state, eval, depth));
        return tree;
    }

    private float expectimax(Map<SimpleLongState, Float> tree, SimpleLongState state, SimpleTrainedEval eval, int depth) {
        if (controller.isGameOver(state)) {
            return controller.score(state);
        }
        if (tree.containsKey(state)) {
            return tree.get(state);
        }
        float score;
        SimpleLongState tmpState = new SimpleLongState(state);
        if (depth == 0) {
            score = Math.max(eval.evaluate(state), controller.score(state));
        } else if (state.isPlayerTurn()) {
            score = Float.NEGATIVE_INFINITY;
            AvailableMoves<Move> moves = controller.availableMoves(state);
            for (Move move : moves.toList()) {
                controller.applyMove(tmpState, move);
                float value = expectimax(tree, tmpState, eval, depth - 1);
                tmpState.copyFrom(state);
                score = Math.max(value, score);
            }
        } else {
            score = 0;
            float totalWeight = 0;
            AvailableMoves<Move> moves = controller.availableMoves(state);
            for (Move move : moves.toList()) {
                controller.applyMove(tmpState, move);
                float value = expectimax(tree, tmpState, eval, depth - 1);
                float weight = moves.moveWeight(move);
                tmpState.copyFrom(state);
                score += value * weight;
                totalWeight += weight;
            }
            score /= totalWeight;
        }
        tree.put(tmpState, score);
        assert Float.isFinite(score);
        return score;
    }
}
