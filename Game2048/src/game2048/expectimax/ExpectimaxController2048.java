package game2048.expectimax;

import game2048.expectimax.eval.SimpleEval;
import game2048.MyMctsController;
import game2048.SimpleLongState;
import game2048.expectimax.eval.Evaluation;
import game2048.mcts.HasWeight;
import game2048.mcts.Move;
import game2048.mymcts.AvailableMoves;
import java.util.Iterator;
import java.util.Random;


public class ExpectimaxController2048 implements ExpectimaxController<Move> {

    private final SimpleLongState state;
    private final MyMctsController controller;
    private final long[] moveBuffer = new long[1024];
    private int bufferPointer = 0;
    private Evaluation eval;
    private final Random rng;

    public ExpectimaxController2048(SimpleLongState state, MyMctsController controller, Random rng) {
        this(state, controller, rng, new SimpleEval(controller));
    }
    public ExpectimaxController2048(SimpleLongState state, MyMctsController controller, Random rng, Evaluation eval) {
        this.state = state;
        this.controller = controller;
        this.eval = eval;
        this.rng = rng;
    }
    
    public void reset(SimpleLongState state) {
        copyState(state);
        resetBuffer();
    }

    private void copyState(SimpleLongState source) {
        state.setBoard(source.getBoard());
        state.setPlayerTurn(source.isPlayerTurn());
    }
    public void resetBuffer() {
        bufferPointer = 0;
    }
    
    @Override
    public void make(Move move) {
        moveBuffer[bufferPointer++] = state.getBoard();
        controller.applyMove(state, move);
    }

    @Override
    public void unmake(Move move) {
        state.setBoard(moveBuffer[--bufferPointer]);
        state.flipPlayerTurn();
    }

    @Override
    public boolean isNextMoveRandom() {
        return !state.isPlayerTurn();
    }

    @Override
    public Iterable<Move> availableMoves() {
        return controller.availableMoves(state).toList();
    }

    @Override
    public int moveWeight(Move move) {
        return ((HasWeight)move).getWeight();
    }

    @Override
    public float eval() {
        return eval.evaluate(state);
    }
    
    @Override
    public boolean isGameOver() {
        return controller.isGameOver(state);
    }

    @Override
    public long id() {
        return controller.getId(state);
    }

    @Override
    public Iterable<Move> sampleMoves(int num) {
        AvailableMoves<Move> moves = controller.availableMoves(state);
        if(moves.count() <= num) {
            return moves.toList();
        }
        return () -> {
            return new Iterator<Move>() {
                int remaining = num;
                @Override
                public boolean hasNext() {
                    return remaining != 0;
                }

                @Override
                public Move next() {
                    remaining--;
                    return moves.selectRandomMove(rng);
                }
            };
        };
    }

}
