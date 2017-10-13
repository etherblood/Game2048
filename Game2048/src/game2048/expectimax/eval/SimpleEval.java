package game2048.expectimax.eval;

import game2048.MyMctsController;
import game2048.SimpleLongState;
import java.util.function.ToIntFunction;

/**
 *
 * @author Philipp
 */
public class SimpleEval implements Evaluation {

    private final MyMctsController controller;

    public SimpleEval(MyMctsController controller) {
        this.controller = controller;
    }

    @Override
    public float evaluate(SimpleLongState state) {
        float score = controller.score(state);
        if (!controller.isGameOver(state)) {
            score += 1000;
        }
        return score;
    }
}
