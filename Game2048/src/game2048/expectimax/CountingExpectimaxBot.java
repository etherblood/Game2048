package game2048.expectimax;

import java.util.List;

/**
 *
 * @author Philipp
 */
public class CountingExpectimaxBot<M> {
    
    private final static boolean VERBOSE = true;
    private final static boolean SAMPLING = false;
    private final static boolean MAX_TT = false, EXPECTI_TT = true;
    private final boolean maxTtEnabled, expectiTtEnabled;
    
    private final ExpectimaxController<M> controller;
    private final TranspositionTable table;
    private final TranspositionEntry entry = new TranspositionEntry();
    private final Zobrist zobrist;
    public float lastScore;

    public CountingExpectimaxBot(ExpectimaxController<M> controller, TranspositionTable table, Zobrist zobrist) {
        this.controller = controller;
        this.table = table;
        this.zobrist = zobrist;
        maxTtEnabled = MAX_TT && table != null;
        expectiTtEnabled = EXPECTI_TT && table != null;
    }
    
    long nodes;
    public M search(int n) {
        if(n <= 0) {
            throw new IllegalArgumentException("nodes must be greater than 0.");
        }
        if(controller.isNextMoveRandom()) {
            throw new IllegalStateException("cannot search random moves.");
        }
        nodes = 1;
//        maxHit = maxMiss = 0;
//        minHit = minMiss = 0;
        long nanos = System.nanoTime();
        float bestScore = Float.NEGATIVE_INFINITY;
        M bestMove = null;
        List<M> availableMoves = controller.availableMoves();
        int nextN = (n - 1) / availableMoves.size();
        for (M move : availableMoves) {
            controller.make(move);
            float moveScore = expecti(nextN);
            controller.unmake(move);
            if(moveScore > bestScore) {
                bestMove = move;
                bestScore = moveScore;
            }
        }
        if (VERBOSE) {
            nanos = System.nanoTime() - nanos;
            if (nanos == 0) {
                nanos++;
            }
            long millis = nanos / 1000000;
            long knps = 1000000 * nodes / nanos;
//            System.out.println("score: " + bestScore + " depth: " + depth);
            System.out.println(nodes + " nodes / " + millis + " ms (" + knps + " kn/s)");
//            System.out.println("branching: " + String.format("%s", Math.pow(nodes, 1d / depth)));
            System.out.println("max hits: " + maxHit + " misses: " + maxMiss);
            System.out.println("expecti hits: " + minHit + " misses: " + minMiss);
        }
        lastScore = bestScore;
        return bestMove;
    }
    
    long maxHit, maxMiss;
    private float max(int n) {
        assert !controller.isNextMoveRandom();
        nodes++;
        if(n <= 1) {
            return controller.eval();
        }
      
        long id = 0, hash = 0;
        if (maxTtEnabled) {
            id = controller.id();
            hash = zobrist.hash(id);
            table.load(hash, entry);
            if (entry.getId() == id && entry.getDepth() >= n) {
                maxHit++;
                return entry.getScore();
            }
            maxMiss++;
        }
        
        float score = Float.NEGATIVE_INFINITY;
        List<M> availableMoves = controller.availableMoves();
        int nextN = (n - 1) / availableMoves.size();
        for (M move : availableMoves) {
            controller.make(move);
            float moveScore = expecti(nextN);
            controller.unmake(move);
            
            score = Math.max(moveScore, score);
        }
        if(maxTtEnabled) {
            entry.setDepthAndScore(n, (int)score);
            entry.setId(id);
            table.save(hash, entry);
        }
        return score;
    }
    
    long minHit, minMiss;
    private float expecti(int n) {
        assert controller.isNextMoveRandom();
        nodes++;
        if(n <= 1|| controller.isGameOver()) {
            return controller.eval();
        }
        
        long id = 0, hash = 0;
        if (expectiTtEnabled) {
            id = controller.id();
            hash = zobrist.hash(id);
            table.load(hash, entry);
            if (entry.getId() == id && entry.getDepth() >= n) {
                minHit++;
                return entry.getScore();
            }
            minMiss++;
        }
        
        int weightSum = 0;
        int scoreSum = 0;
        int score;
        List<M> availableMoves = controller.availableMoves();
        for (M availableMove : availableMoves) {
            weightSum += controller.moveWeight(availableMove);
        }
        int nextN = (n - 1);
        for (M move : /*SAMPLING? controller.sampleMoves(1 * depth):*/ availableMoves) {
            int weight = controller.moveWeight(move);
            
            controller.make(move);
            float moveScore = max(nextN  * weight / weightSum);
            controller.unmake(move);
            
            scoreSum += weight * moveScore;
        }
        score = scoreSum / weightSum;
        if(expectiTtEnabled) {
            entry.setDepthAndScore(n, score);
            entry.setId(id);
            table.save(hash, entry);
        }
        return score;
    }
}
