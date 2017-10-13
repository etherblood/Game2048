package game2048.expectimax;

/**
 *
 * @author Philipp
 */
public class ExpectimaxBot<M> {
    
    private final static boolean VERBOSE = false;
    private final static boolean SAMPLING = false;
    private final static boolean MAX_TT = false, EXPECTI_TT = true;
    private final boolean ttEnabled;
    
    private final ExpectimaxController<M> controller;
    private final TranspositionTable table;
    private final TranspositionEntry entry = new TranspositionEntry();
    private final Zobrist zobrist;
    public float lastScore;

    public ExpectimaxBot(ExpectimaxController<M> controller, TranspositionTable table, Zobrist zobrist) {
        this.controller = controller;
        this.table = table;
        this.zobrist = zobrist;
        ttEnabled = table != null;
    }
    
    long nodes;
    public M search(int depth) {
        if(depth <= 0) {
            throw new IllegalArgumentException("depth must be greater than 0.");
        }
        if(controller.isNextMoveRandom()) {
            throw new IllegalStateException("cannot search random moves.");
        }
        nodes = 0;
        maxHit = maxMiss = 0;
        long nanos = System.nanoTime();
        float bestScore = Float.NEGATIVE_INFINITY;
        M bestMove = null;
        for (M move : controller.availableMoves()) {
            controller.make(move);
            float moveScore = expecti(depth - 1);
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
            System.out.println("score: " + bestScore + " depth: " + depth);
            System.out.println(nodes + " nodes / " + millis + " ms (" + knps + " kn/s)");
            System.out.println("branching: " + String.format("%s", Math.pow(nodes, 1d / depth)));
            System.out.println("max hits: " + maxHit + " misses: " + maxMiss);
            System.out.println("expecti hits: " + minHit + " misses: " + minMiss);
        }
        lastScore = bestScore;
        return bestMove;
    }
    
    long maxHit, maxMiss;
    private float max(int depth) {
        assert !controller.isNextMoveRandom();
        nodes++;
        if(depth <= 0) {
            return controller.eval();
        }
      
        long id = 0, hash = 0;
        if (MAX_TT && ttEnabled) {
            id = controller.id();
            hash = zobrist.hash(id);
            table.load(hash, entry);
            if (entry.getId() == id && entry.getDepth() >= depth) {
                maxHit++;
                return entry.getScore();
            }
            maxMiss++;
        }
        
        float score = Float.NEGATIVE_INFINITY;
        
        for (M move : controller.availableMoves()) {
            controller.make(move);
            float moveScore = expecti(depth - 1);
            controller.unmake(move);
            
            score = Math.max(moveScore, score);
        }
        if(MAX_TT && ttEnabled) {
            entry.setDepthAndScore(depth, (int)score);
            entry.setId(id);
            table.save(hash, entry);
        }
        return score;
    }
    
    long minHit, minMiss;
    private float expecti(int depth) {
        assert controller.isNextMoveRandom();
        nodes++;
        if(depth <= 0|| controller.isGameOver()) {
            return controller.eval();
        }
        
        long id = 0, hash = 0;
        if (EXPECTI_TT && ttEnabled) {
            id = controller.id();
            hash = zobrist.hash(id);
            table.load(hash, entry);
            if (entry.getId() == id && entry.getDepth() >= depth) {
                minHit++;
                return entry.getScore();
            }
            minMiss++;
        }
        
        int weightSum = 0;
        int scoreSum = 0;
        int score;
        
        for (M move : SAMPLING? controller.sampleMoves(1 * depth): controller.availableMoves()) {
            int weight = controller.moveWeight(move);
            
            controller.make(move);
            float moveScore = max(depth - (weight == 1? 2: 1));
            controller.unmake(move);
            
            weightSum += weight;
            scoreSum += weight * moveScore;
        }
        score = scoreSum / weightSum;
        if(EXPECTI_TT && ttEnabled) {
            entry.setDepthAndScore(depth, score);
            entry.setId(id);
            table.save(hash, entry);
        }
        return score;
    }
}
