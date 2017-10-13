package game2048;

import game2048.mcts.HasWeight;
import game2048.mcts.Move;
import game2048.mymcts.AvailableMoves;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class AvailableSetSquareValueMoves implements AvailableMoves<Move> {

    private final long occupied;

    public AvailableSetSquareValueMoves(long occupied) {
        this.occupied = occupied;
    }
    
    @Override
    public boolean isMoveSelectedRandomly() {
        return true;
    }

    @Override
    public Move selectRandomMove(Random rng) {
        int numFree = 16 - Long.bitCount(occupied);
        int selected = rng.nextInt(10 * numFree);
        int freeSquare = selected / 10;
        long freeSquares = ~(0xe * Masks.ALL_SQUARES | occupied);
        for (int i = 0; i < freeSquare; i++) {
            freeSquares &= freeSquares - 1;
        }
        int square = Long.numberOfTrailingZeros(freeSquares) / 4;
        if((freeSquare * 10) == selected) {
            return new SetSquareValueMove(square, 2, 1);
        }
        return new SetSquareValueMove(square, 1, 9);
//        long current = occupied;
//        for (int i = 0; i < 16; i++) {
//            if((current & 1) == 0) {
//                if(freeSquare == 0) {
//                    return new SetSquareValueMove(i, value, value == 1? 9: 1);
//                }
//                freeSquare--;
//            }
//            current >>>= 4;
//        }
//        throw new IllegalStateException();
    }

    @Override
    public List<Move> toList() {
        List<Move> moves = new ArrayList<>();
        long current = occupied;
        for (int i = 0; i < 16; i++) {
            if((current & 1) == 0) {
                moves.add(new SetSquareValueMove(i, 1, 9));
                moves.add(new SetSquareValueMove(i, 2, 1));
            }
            current >>>= 4;
        }
        return moves;
    }

    @Override
    public int totalWeight() {
        return 5 * count();
    }

    @Override
    public int count() {
        return 2 * (16 - Long.bitCount(occupied));
    }

    @Override
    public int moveWeight(Move move) {
        return ((HasWeight)move).getWeight();
    }

}
