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
public class SimpleTrainedEval implements Evaluation {

    public SimpleTrainedEval() {
        for (int value = 0; value < all_scores.length; value++) {
            all_scores[value] = new TrainedValue();
        }
        normalize();
    }

    public TrainedValue[] all_scores = new TrainedValue[1 << 16];

    @Override
    public float evaluate(SimpleLongState state) {
        float score = 0;
        score += all_scores[state.getColumnFlipped(0)].get();
        score += all_scores[state.getColumnFlipped(1)].get();
        score += all_scores[state.getColumnFlipped(2)].get();
        score += all_scores[state.getColumnFlipped(3)].get();
        score += all_scores[state.getRow(0)].get();
        score += all_scores[state.getRow(1)].get();
        score += all_scores[state.getRow(2)].get();
        score += all_scores[state.getRow(3)].get();
        return score;
    }


    public ParameterVector<TrainedValue> featureVecorOf(SimpleLongState state) {
        ParameterVector<TrainedValue> vector = new ParameterVector<>();
        for (int x = 0; x < 4; x++) {
            int col = state.getColumnFlipped(x);
            TrainedValue value = all_scores[col];
            vector.add(value, 1);
        }
        for (int y = 0; y < 4; y++) {
            int row = state.getRow(y);
            TrainedValue value = all_scores[row];
            vector.add(value, 1);
        }
        return vector;
    }

    private void normalize() {
        for (int value = 0; value < all_scores.length; value++) {
            int flipped = flip(value);
            all_scores[value] = all_scores[flipped];
        }
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
        for (TrainedValue score : all_scores) {
            out.writeFloat(score.get());
        }
    }

    public void load(DataInputStream in) throws IOException {
        for (TrainedValue score : all_scores) {
            score.set(in.readFloat());
        }
    }
}
