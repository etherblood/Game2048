package game2048.expectimax.eval;

import game2048.SimpleLongState;
import game2048.expectimax.eval.treestrap.ParameterVector;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Philipp
 */
public class TrainedEval implements Evaluation {

    private final static boolean USE_PLAYER_TURN = true;

    public TrainedEval() {
        for (TrainedValue[][] scores : all_scores) {
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < scores[j].length; i++) {
                    scores[j][i] = new TrainedValue();
                }
            }
        }
        for (int i = 0; i < all_playerMoves.length; i++) {
            all_playerMoves[i] = new TrainedValue();
        }
        normalize();
    }

    public TrainedValue[][][] all_scores = new TrainedValue[17][2][1 << 16];
    public TrainedValue[] all_playerMoves = new TrainedValue[17];
//    float[] early_scores = new float[1 << 16], late_scores = new float[1 << 16];
//    float early_playerMove, late_playerMove;

    @Override
    public float evaluate(SimpleLongState state) {
        return floatScore(state);
    }

    public float floatScore(SimpleLongState state) {
        int iPhase = getPhase(state);
        return phaseScore(state, all_scores[iPhase], all_playerMoves[iPhase]);
//        float phase = controller.countOccupiedSquares(state) / 16f;
//        float early = phaseScore(state, early_scores, early_playerMove);
//        float late = phaseScore(state, late_scores, late_playerMove);
//        return (1 - phase) * early + phase * late;
    }

    private int getPhase(SimpleLongState state) {
        return highestTile(state);
    }
    
    private int highestTile(SimpleLongState state) {
        int max = 0;
        for (int square = 0; square < 16; square++) {
            max = Math.max(max, state.getSquare(square));
        }
        return max;
    }

    private float phaseScore(SimpleLongState state, TrainedValue[][] scores, TrainedValue playerMove) {
        float score = 0;
        score += scores[0][state.getColumnFlipped(0)].get();
        score += scores[1][state.getColumnFlipped(1)].get();
        score += scores[1][state.getColumnFlipped(2)].get();
        score += scores[0][state.getColumnFlipped(3)].get();

        score += scores[0][state.getRow(0)].get();
        score += scores[1][state.getRow(1)].get();
        score += scores[1][state.getRow(2)].get();
        score += scores[0][state.getRow(3)].get();
        
        if (USE_PLAYER_TURN && state.isPlayerTurn()) {
            score += playerMove.get();
        }
        return score;
    }

//    public void trainAll(float score, float weight) {
//        for (TrainedValue[] scores : all_scores) {
//            for (TrainedValue s : scores) {
//                s.train(score, weight);
//            }
//        }
//        for (TrainedValue all_playerMove : all_playerMoves) {
//            all_playerMove.train(score, weight);
//        }
//    }
    public void train(SimpleLongState state, float score, float stepsize) {
//        weight /= 9;
//        if (controller.isGameOver(state)) {
//            return;
//        }
        float evalScore = floatScore(state);
        float diff = score - evalScore;

        int iPhase = getPhase(state);
//        float phase = controller.countOccupiedSquares(state) / 16f;
//        float earlyMultiplier = (1 - phase) * multiplier;
//        float lateMultiplier = phase * multiplier;
//        float earlyDiff = earlyMultiplier * diff;
//        float lateDiff = lateMultiplier * diff;
        float step = Math.signum(diff) * stepsize;

        for (int x = 0; x < 4; x++) {
            int col = state.getColumnFlipped(x);
            all_scores[iPhase][Integer.bitCount(x) & 1][col].inc(step);
//            early_scores[col] += earlyDiff;
//            late_scores[col] += lateDiff;
        }
        for (int y = 0; y < 4; y++) {
            int row = state.getRow(y);
            all_scores[iPhase][Integer.bitCount(y) & 1][row].inc(step);
//            early_scores[row] += earlyDiff;
//            late_scores[row] += lateDiff;
        }
        if (USE_PLAYER_TURN && state.isPlayerTurn()) {
            all_playerMoves[iPhase].inc(step);
//            early_playerMove += earlyDiff;
//            late_playerMove += lateDiff;
        }
    }

    public ParameterVector<TrainedValue> featureVecorOf(SimpleLongState state) {
        int iPhase = getPhase(state);
        ParameterVector<TrainedValue> vector = new ParameterVector<>();
         for (int x = 0; x < 4; x++) {
            int col = state.getColumnFlipped(x);
            TrainedValue value = all_scores[iPhase][Integer.bitCount(x) & 1][col];
            vector.add(value, 1);
        }
        for (int y = 0; y < 4; y++) {
            int row = state.getRow(y);
            TrainedValue value = all_scores[iPhase][Integer.bitCount(y) & 1][row];
            vector.add(value, 1);
        }
        if (USE_PLAYER_TURN && state.isPlayerTurn()) {
            TrainedValue value = all_playerMoves[iPhase];
            vector.add(value, 1);
        }
        return vector;
    }

    private void normalize() {
        for (TrainedValue[][] scores : all_scores) {
            for (int j = 0; j < 2; j++) {

                for (int a = 0; a < scores[j].length; a++) {
                    int b = flip(a);

                    scores[j][a] = scores[j][b];
                }
            }
        }
//        for (int a = 0; a < early_scores.length; a++) {
//            int b = flip(a);
//            
//            float score = (early_scores[a] + early_scores[b]) / 2;
//            early_scores[a] = score;
//            early_scores[b] = score;
//            
//            score = (late_scores[a] + late_scores[b]) / 2;
//            late_scores[a] = score;
//            late_scores[b] = score;
//        }
    }

    private int flip(int value) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result <<= 4;
            result |= value & 0xf;
            value >>>= 4;
        }
        return result;
    }

    public void save(DataOutputStream out) throws IOException {
        for (TrainedValue[][] scores : all_scores) {
            for (int i = 0; i < 2; i++) {
                for (TrainedValue score : scores[i]) {
                    out.writeFloat(score.get());
                }
            }
        }
        for (TrainedValue playerMove : all_playerMoves) {
            out.writeFloat(USE_PLAYER_TURN ? playerMove.get() : 0);
        }
    }

    public void load(DataInputStream in) throws IOException {
        for (TrainedValue[][] scores : all_scores) {
            for (int i = 0; i < 2; i++) {
                for (TrainedValue score : scores[i]) {
                    score.set(in.readFloat());
                }
            }
        }
        for (TrainedValue all_playerMove : all_playerMoves) {
            all_playerMove.set(USE_PLAYER_TURN ? in.readFloat() : 0);
        }
    }
}
