package game2048.expectimax.eval;

import game2048.MyMctsController;
import game2048.SimpleLongState;

/**
 *
 * @author Philipp
 */
public class AdvancedEval implements Evaluation {
    private final MyMctsController controller;

    private static final float SCORE_LOST_PENALTY = 200000.0f;
    private static final float SCORE_MONOTONICITY_POWER = 4.0f;
    private static final float SCORE_MONOTONICITY_WEIGHT = 47.0f;
    private static final float SCORE_SUM_POWER = 3.5f;
    private static final float SCORE_SUM_WEIGHT = 11.0f;
    private static final float SCORE_MERGES_WEIGHT = 700.0f;
    private static final float SCORE_EMPTY_WEIGHT = 270.0f;

    private static float[] heur_score_table = new float[65536];

    public AdvancedEval(MyMctsController controller) {
        this.controller = controller;
    }

    static {
        for (int row = 0; row < 65536; ++row) {
            int[] line = {
                (row >> 0) & 0xf,
                (row >> 4) & 0xf,
                (row >> 8) & 0xf,
                (row >> 12) & 0xf
            };
            // Heuristic score
            float sum = 0;
            int empty = 0;
            int merges = 0;

            int prev = 0;
            int counter = 0;
            for (int i = 0; i < 4; ++i) {
                int rank = line[i];
                sum += Math.pow(rank, SCORE_SUM_POWER);
                if (rank == 0) {
                    empty++;
                } else {
                    if (prev == rank) {
                        counter++;
                    } else if (counter > 0) {
                        merges += 1 + counter;
                        counter = 0;
                    }
                    prev = rank;
                }
            }
            if (counter > 0) {
                merges += 1 + counter;
            }

            float monotonicity_left = 0;
            float monotonicity_right = 0;
            for (int i = 1; i < 4; ++i) {
                if (line[i - 1] > line[i]) {
                    monotonicity_left += Math.pow(line[i - 1], SCORE_MONOTONICITY_POWER) - Math.pow(line[i], SCORE_MONOTONICITY_POWER);
                } else {
                    monotonicity_right += Math.pow(line[i], SCORE_MONOTONICITY_POWER) - Math.pow(line[i - 1], SCORE_MONOTONICITY_POWER);
                }
            }

            heur_score_table[row] = SCORE_LOST_PENALTY
                    + SCORE_EMPTY_WEIGHT * empty
                    + SCORE_MERGES_WEIGHT * merges
                    - SCORE_MONOTONICITY_WEIGHT * Math.min(monotonicity_left, monotonicity_right)
                    - SCORE_SUM_WEIGHT * sum;

        }
    }

    @Override
    public float evaluate(SimpleLongState state) {
        if(controller.isGameOver(state)) {
            return (int) controller.score(state);
        }
        float score = 0;
        for (int x = 0; x < 4; x++) {
            score += heur_score_table[state.getColumnFlipped(x)];
        }
        for (int y = 0; y < 4; y++) {
            score += heur_score_table[state.getRow(y)];
        }
//        if (state.isPlayerTurn()) {
//            score += playerMove.get();
//        }
        return score;
    }
}
