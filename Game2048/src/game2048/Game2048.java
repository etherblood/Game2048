package game2048;

import game2048.expectimax.ExpectimaxBot;
import game2048.expectimax.ExpectimaxController;
import game2048.expectimax.ExpectimaxController2048;
import game2048.expectimax.TranspositionTable;
import game2048.expectimax.Zobrist;
import game2048.expectimax.eval.AdvancedEval;
import game2048.expectimax.eval.Evaluation;
import game2048.expectimax.eval.TrainedEval;
import game2048.mcts.HasWeight;
import game2048.mcts.MonteCarloAgent;
import game2048.mcts.MonteCarloNode;
import game2048.mcts.Move;
import game2048.mymcts.AvailableMoves;
import game2048.mymcts.MctsAgent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philipp
 */
public class Game2048 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
//        playerPlay();
//        randomPlay();
//        myMctsPlay();
//        expectimaxPlay();
        trainEval2();
    }

    private static void trainEval2() throws IOException {
        long seed = System.nanoTime();
        MyMctsController controller = new MyMctsController();
        SimpleLongState state = new SimpleLongState();
        Random rng = new Random(seed);
        TrainedEval trainedEval = new TrainedEval();
        File file = Paths.get("rng_traineval.dat").toFile();
        if (file.exists()) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                trainedEval.load(in);
            }
        }
        ExpectimaxController2048 c2 = new ExpectimaxController2048(state, controller, rng, trainedEval);
        int i = 0;
        int trigger = 10000000;
        while (true) {
            i++;
            if (i % trigger == 0) {
                state.setBoard(0);
                state.setPlayerTurn(false);
            } else {
                state.setBoard(rng.nextLong() & rng.nextLong() & rng.nextLong());
                state.setPlayerTurn(rng.nextBoolean());
            }
            float score;
            try {
                c2.reset(state);
                score = expectimaxScore(c2, 1);
            } catch (LongStateOverflowException e) {
                score = 1000000 + controller.score(state);
            }
            trainedEval.train(state, score, 0.1f);
            if (i % trigger == 0) {
                System.out.println("startEval: " + trainedEval.evaluate(state));
                file.createNewFile();
                try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                    trainedEval.save(out);
                }
                System.out.println("saved");
                expectimaxPlay(trainedEval, 5);
                System.out.println();
            }
        }
    }

    private static void trainEval() throws IOException {
        long seed = System.nanoTime();
        MyMctsController controller = new MyMctsController();
        Random rng = new Random(seed);
        TrainedEval trainedEval = new TrainedEval();

        File file = Paths.get("traineval.dat").toFile();
        if (file.exists()) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                trainedEval.load(in);
            }
        } else {
//            trainedEval.bootstrap();
        }

        ExpectimaxController2048 expectimaxController2048 = new ExpectimaxController2048(new SimpleLongState(), controller, rng, trainedEval);
        TranspositionTable table = new TranspositionTable(20);
        ExpectimaxBot<Move> bot = new ExpectimaxBot<>(expectimaxController2048, table, new Zobrist(rng));
        SimpleLongState state = new SimpleLongState();

        for (int i = 1;; i++) {
            AvailableDirectionMoves.ALL.sort(Comparator.comparingInt(o -> rng.nextInt()));
//            boolean rngRun = rng.nextInt(10) == 0;
//            long s = 0;
//            if (rngRun) {
//                s = rng.nextLong();
            long v = ~0;
            for (int j = 0; j < 5; j++) {
                v &= rng.nextLong();
            }
            state.setBoard(v);
//            state.setBoard(state.getBoard() & rng.nextLong());
//                long s = state.getBoard();
//                if(rng.nextBoolean()) {
//                    s = 0;
//                } else {
//                    s >>>= 16;
//                } 
//                while (rng.nextBoolean()) {
////                    s &= rng.nextLong();
//                    int square = rng.nextInt(16);
//                    int value = rng.nextInt(16);
//                    state.setSquare(square, value);
//                }
//            }
//            state.setBoard(s);
            state.setPlayerTurn(false);
            Map<SimpleLongState, Double> history = new LinkedHashMap<>();
//            double weight = 1;
            history.put(new SimpleLongState(state), 1d);
//            SimpleLongState startState = new SimpleLongState();
//            startState.copyFrom(state);
            System.out.println("running match " + Long.toHexString(state.getBoard()));
            boolean overflow = false;
            try {
                while (!controller.isGameOver(state)) {
                    Move move;
                    double d = 1;
                    if (state.isPlayerTurn()) {
                        expectimaxController2048.reset(state);
                        move = bot.search(5);
                    } else {
                        AvailableMoves<Move> availableMoves = controller.availableMoves(state);
                        double totalWeight = availableMoves.totalWeight();
                        move = availableMoves.selectRandomMove(rng);
                        double moveWeight = ((HasWeight) move).getWeight();
                        d = moveWeight / totalWeight;
                    }
                    controller.applyMove(state, move);
                    history.put(new SimpleLongState(state), d);

//                System.out.println();
                }
            } catch (LongStateOverflowException e) {
                System.out.println("overflow");
                overflow = true;
            }
//            System.out.println("state: " + Long.toHexString(state.getBoard()));
            controller.printState(state, System.out);
            float score = controller.score(state);
            if (overflow) {
                score += 1 << 16;
            }
            System.out.println("score is: " + (int) score);
            System.out.println("training " + i + "th time");

            double weight = 1;
            List<Entry<SimpleLongState, Double>> list = new ArrayList<>(history.entrySet());
            for (int j = list.size() - 1; j >= 0 && weight > 0; j--) {
                Entry<SimpleLongState, Double> entry = list.get(j);
                SimpleLongState trainState = entry.getKey();
//                double weightMultiplier = entry.getValue();
//                weight *= 0.99f;
                expectimaxController2048.reset(trainState);
                float sc = expectimaxScore(expectimaxController2048, 1);
                trainedEval.train(trainState, sc, 0.1f);
            }

//            for (Map.Entry<SimpleLongState, Double> entry : history.entrySet()) {
//                float stateWeight = (float) (entry.getValue() / weight);
//                trainedEval.train(entry.getKey(), score, stateWeight);
//            }
//            train(history, trainedEval, startState, score, controller);
//            if (i % 10 == 0) {
            table.clear();
            System.out.println("table reset");
//                trainedEval.trainAll(1000000, 0.001f);
//                trainedEval.normalize();
//                trainedEval.inc();
//                System.out.println("normalized");
//            }
            if (i % 100 == 0) {
                file.createNewFile();
                try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                    trainedEval.save(out);
                }
                System.out.println("saved");
            }

//            state.setBoard(0);
//            state.setPlayerTurn(false);
//            expectimaxController2048.reset(state);
//            System.out.println("bot score of empty board is: " + bot.expecti(5));
            System.out.printf("eval score of empty board is: %.2f", trainedEval.floatScore(new SimpleLongState()));
            System.out.println();
        }
    }

    private static float expectimaxScore(ExpectimaxController2048 controller, int depth) {
        if (depth == 0 || controller.isGameOver()) {
            return controller.eval();
        }
        if (controller.isNextMoveRandom()) {
            float weightedSum = 0, weight = 0;
            for (Move move : controller.availableMoves()) {
                float w = controller.moveWeight(move);
                weight += w;
                controller.make(move);
                weightedSum += w * expectimaxScore(controller, depth - 1);
                controller.unmake(move);
            }
            return weightedSum / weight;
        }
        float max = Float.NEGATIVE_INFINITY;
        for (Move move : controller.availableMoves()) {
            controller.make(move);
            max = Math.max(max, expectimaxScore(controller, depth - 1));
            controller.unmake(move);
        }
        return max;
    }

//    private static void train(List<Move> history, TrainedEval trainedEval, SimpleLongState startState, float score, MyMctsController controller) {
//        SimpleLongState state = new SimpleLongState();
////        float multiplier = 1/9f;
////        multiplier /= history.size() + 1;
//        for (int i = 0; i < 1; i++) {
//            state.copyFrom(startState);
//            trainedEval.train(state, score, multiplier);
//            for (Move move : history) {
//                controller.applyMove(state, move);
//                trainedEval.train(state, score, multiplier);
//            }
//        }
//        System.out.println("trained");
//    }
    private static void expectimaxPlay(Evaluation eval, int depth) {
        long seed = System.nanoTime();
        try {
            MyMctsController controller = new MyMctsController();
            Random rng = new Random(seed);
            ExpectimaxController2048 expectimaxController2048 = new ExpectimaxController2048(new SimpleLongState(), controller, rng, eval);
            TranspositionTable table = new TranspositionTable(25);
            ExpectimaxBot<Move> bot = new ExpectimaxBot<>(expectimaxController2048, table, new Zobrist(rng));
            SimpleLongState state = new SimpleLongState();
            state.setBoard(1);
            state.setPlayerTurn(true);

            while (!controller.isGameOver(state)) {
                Move move;
                if (state.isPlayerTurn()) {
                    expectimaxController2048.reset(state);
                    move = bot.search(depth);
                } else {
                    move = controller.availableMoves(state).selectRandomMove(rng);
                }
                controller.applyMove(state, move);

//                controller.printState(state, System.out);
//                System.out.println();
            }
                controller.printState(state, System.out);
            System.out.println("score is: " + (int) controller.score(state));
        } finally {
            System.out.println("seed was: " + seed);
        }
    }

    private static int countUniqueTiles(SimpleLongState state) {
        int flags = 0;
        for (int square = 0; square < 16; square++) {
            flags |= 1 << state.getSquare(square);
        }
        return Integer.bitCount(flags);
    }

    private static void myMctsPlay() {
        long seed = System.nanoTime();
        MyMctsController controller = new MyMctsController();
        Random rng = new Random(seed);
        MctsAgent<SimpleLongState, Move, Long> agent = new MctsAgent<>(rng, controller, new SimpleLongState());
        SimpleLongState state = new SimpleLongState();
        state.setBoard(1);
        state.setPlayerTurn(true);

        while (!controller.isGameOver(state)) {
            Move move;
            if (state.isPlayerTurn()) {
                agent.removeUnreachableTranspositions();
//                long end = System.nanoTime() + 1000 * 1000 * 1000;
//                while(System.nanoTime() < end) {
                while (agent.simulationStrength(state) < 100000) {
                    agent.iteration(state);
                }
                move = agent.bestChild(state);
            } else {
                move = controller.availableMoves(state).selectRandomMove(rng);
            }
            controller.applyMove(state, move);
            agent.onMoveApplied(state, move);

            controller.printState(state, System.out);
            System.out.println();
        }
        System.out.println("score is: " + (int) controller.score(state));
        System.out.println("seed was: " + seed);
    }

    private static void mctsPlay() {
        Mcts2048 mcts = new Mcts2048();
        MonteCarloAgent<Mcts2048, Move> agent = new MonteCarloAgent<>(mcts, new MonteCarloNode<>(1));

        agent.iteration(mcts);
        LongState state = new LongState();
        Random rng = new Random(1);
        LongStateController controller = new LongStateController();
        try (Scanner s = new Scanner(System.in)) {
            while (controller.placeNewValue(state, rng) && hasMove(controller, state)) {
                controller.printState(state, System.out);
                int direction = s.nextInt();
                controller.move(state, direction);
            }
        }
        controller.printState(state, System.out);
    }

    private static void playerPlay() {
        LongState state = new LongState();
        Random rng = new Random(1);
        LongStateController controller = new LongStateController();
        try (Scanner s = new Scanner(System.in)) {
            while (controller.placeNewValue(state, rng) && hasMove(controller, state)) {
                controller.printState(state, System.out);
                int direction = s.nextInt();
                controller.move(state, direction);
            }
        }
        controller.printState(state, System.out);
    }

    private static void randomPlay() {
        LongState state = new LongState();
        long seed = System.currentTimeMillis();
        Random rng = new Random(seed);
        LongStateController controller = new LongStateController();
        while (controller.placeNewValue(state, rng) && hasMove(controller, state)) {
            controller.printState(state, System.out);
            int direction = rng.nextInt(4);
            System.out.println(direction);
            controller.move(state, direction);
        }
        controller.printState(state, System.out);
        System.out.println("score: " + controller.score(state));
        System.out.println("GAME OVER, used seed was: " + seed);
    }

    private static boolean hasMove(LongStateController controller, LongState state) {
        long prev = state.getState();
        for (int direction = 0; direction < 4; direction++) {
            controller.move(state, direction);
            if (state.getState() != prev) {
                state.setState(prev);
                return true;
            }
            state.setState(prev);
        }
        return false;
    }

}
