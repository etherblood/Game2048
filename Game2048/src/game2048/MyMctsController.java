package game2048;

import game2048.mcts.Move;
import game2048.mymcts.AvailableMoves;
import game2048.mymcts.MctsStateController;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class MyMctsController implements MctsStateController<SimpleLongState, Move, Long> {

    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    public static final int OVERFLOW = 0xffff;

    public static final int[] LEFT_MOVES, RIGHT_MOVES, SCORES;

    static {
        int[] flipValueMap = new int[1 << 16];
        for (int value = 0; value < flipValueMap.length; value++) {
            for (int i = 0; i < 4; i++) {
                int shift = 4 * i;
                flipValueMap[value] |= ((value >>> shift) & 0xf) << (12 - shift);
            }
            assert Integer.bitCount(flipValueMap[value]) == Integer.bitCount(value);
        }
        LEFT_MOVES = new int[1 << 16];
        for (int value = 0; value < LEFT_MOVES.length; value++) {
            int[] map = new int[4];
            for (int i = 0; i < 4; i++) {
                map[i] = (value >>> (4 * i)) & 0xf;
            }

            int leftMost = 0;
            for (int i = 1; i < 4; i++) {
                int current = map[i];
                if (current != 0) {
                    for (int j = i - 1; j >= leftMost; j--) {
                        if (map[j] == 0) {
                            map[j] = map[j + 1];
                            map[j + 1] = 0;
                        } else if (map[j] == map[j + 1]) {
                            leftMost = j + 1;
                            map[j]++;
                            map[j + 1] = 0;
                        }
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                if (map[i] >= 16) {
                    LEFT_MOVES[value] = OVERFLOW;
                    break;
                }
                LEFT_MOVES[value] |= map[i] << (4 * i);
            }
            assert LEFT_MOVES[value] == OVERFLOW || sumSquares(value) == sumSquares(LEFT_MOVES[value]) : Integer.toHexString(value) + "->" + Integer.toHexString(LEFT_MOVES[value]);
        }
        RIGHT_MOVES = new int[1 << 16];
        for (int value = 0; value < RIGHT_MOVES.length; value++) {
            int movedLeft = LEFT_MOVES[value];
            int flippedValue = flipValueMap[value];
            RIGHT_MOVES[flippedValue] = movedLeft == OVERFLOW ? OVERFLOW : flipValueMap[movedLeft];
            assert RIGHT_MOVES[flippedValue] == OVERFLOW || sumSquares(flippedValue) == sumSquares(RIGHT_MOVES[flippedValue]) : Integer.toHexString(flippedValue) + "->" + Integer.toHexString(RIGHT_MOVES[flippedValue]);
        }
        SCORES = new int[1 << 16];
        int[] scoreHelper = new int[16];
        for (int i = 2; i < 16; i++) {
            scoreHelper[i] = scoreHelper[i - 1] + (1 << i);
        }
        for (int value = 0; value < SCORES.length; value++) {
            int score = 0;
            for (int i = 0; i < 4; i++) {
                score += scoreHelper[(value >>> (4 * i)) & 0xf];
            }
            SCORES[value] = score;
        }
    }

    private static int sumSquares(int value) {
        assert value == (value & 0xffff);

        int score = 0;
        for (int i = 0; i < 16; i += 4) {
            int squareValue = (value >>> i) & 0xf;
            if (squareValue != 0) {
                score += 1 << squareValue;
            }
        }
        return score;
    }

    public void move(SimpleLongState state, int direction) {
        switch (direction) {
            case UP:
                for (int x = 0; x < 4; x++) {
                    int column = state.getColumnFlipped(x);
                    column = moveDown(column);
                    state.setColumnFlipped(x, column);
                }
                break;
            case RIGHT:
                for (int y = 0; y < 4; y++) {
                    int row = state.getRow(y);
                    row = moveUp(row);
                    state.setRow(y, row);
                }
                break;
            case DOWN:
                for (int x = 0; x < 4; x++) {
                    int column = state.getColumnFlipped(x);
                    column = moveUp(column);
                    state.setColumnFlipped(x, column);
                }
                break;
            case LEFT:
                for (int y = 0; y < 4; y++) {
                    int row = state.getRow(y);
                    row = moveDown(row);
                    state.setRow(y, row);
                }
                break;
        }
    }

    public boolean placeNewValue(SimpleLongState state, Random rng) {
        int square = randomEmptySquare(state, rng);
        if (square == -1) {
            return false;
        }
        int value = rng.nextInt(10) == 0 ? 2 : 1;
        state.setSquare(square, value);
        return true;
    }

    public void printState(SimpleLongState state, PrintStream out) {
        for (int y = 3; y >= 0; y--) {
            for (int x = 0; x < 4; x++) {
                int score = 1 << state.getSquare(state.squareIndex(x, y));
                if(score == 1) {
                    score = 0;
                }
                out.print(score + "\t");
            }
            out.println();
        }
    }

    public int randomEmptySquare(SimpleLongState state, Random rng) {
        long stateValue = state.getBoard();
        int count = 0;
        int current = -4;
        for (int i = 0; i < 64; i += 4) {
            long squareValue = (stateValue >>> i) & 0xf;
            if (squareValue == 0) {
                count++;
                if (rng.nextInt(count) == 0) {
                    current = i;
                }
            }
        }
        return current / 4;
    }

    public int countOccupiedSquares(SimpleLongState state) {
        return Long.bitCount(occupiedSquareFlags(state.getBoard()));
    }

    public int countUniqueTiles(SimpleLongState state) {
        int flags = 0;
        for (int square = 0; square < 16; square++) {
            flags |= 1 << state.getSquare(square);
        }
        return Integer.bitCount(flags);
    }

    public int highestTile(SimpleLongState state) {
        int max = 0;
        for (int square = 0; square < 16; square++) {
            max = Math.max(max, state.getSquare(square));
        }
        return max;
    }

    public boolean hasEmptySquare(SimpleLongState state) {
        return occupiedSquareFlags(state.getBoard()) != Masks.ALL_SQUARES;
    }

    private long occupiedSquareFlags(long mask) {
        mask |= mask >>> 2;
        mask &= 3 * Masks.ALL_SQUARES;
        mask |= mask >>> 1;
        mask &= Masks.ALL_SQUARES;
        return mask;
    }

    protected int moveDown(int value) {
        assert value == (value & 0xffff);

        int result = LEFT_MOVES[value];
        if (result == OVERFLOW) {
            throw new LongStateOverflowException();
        }
        return result;
    }

    protected int moveUp(int value) {
        assert value == (value & 0xffff);

        int result = RIGHT_MOVES[value];
        if (result == OVERFLOW) {
            throw new LongStateOverflowException();
        }
        return result;
    }

    @Override
    public void copy(SimpleLongState from, SimpleLongState to) {
        to.setBoard(from.getBoard());
        to.setPlayerTurn(from.isPlayerTurn());
    }

    @Override
    public void applyMove(SimpleLongState state, Move move) {
        if (state.isPlayerTurn()) {
            move(state, ((DirectionMove) move).getDirection());
        } else {
            SetSquareValueMove ssvm = (SetSquareValueMove) move;
            state.setSquare(ssvm.getSquare(), ssvm.getValue());
        }
        state.flipPlayerTurn();
    }

    @Override
    public float score(SimpleLongState state) {
        int score = 0;
        for (int i = 0; i < 4; i++) {
            score += SCORES[state.getRow(i)];
        }
        return score;
    }

    @Override
    public AvailableMoves<Move> availableMoves(SimpleLongState state) {
        if (state.isPlayerTurn()) {
            return AvailableDirectionMoves.INSTANCE;
        }
        return new AvailableSetSquareValueMoves(occupiedSquareFlags(state.getBoard()));
//        List<Move> moves = new ArrayList<>();
//        for (int square = 0; square < 16; square++) {
//            int sqValue = state.getSquare(square);
//            if (sqValue == 0) {
//                moves.add(new SetSquareValueMove(square, 1, 9));
//                moves.add(new SetSquareValueMove(square, 2, 1));
//            }
//        }
//        return new MovesList<>(moves, true);
    }

    @Override
    public boolean isGameOver(SimpleLongState state) {
        return !(state.isPlayerTurn() || hasEmptySquare(state));
    }

    @Override
    public Long getId(SimpleLongState state) {
        if(state.isPlayerTurn()) {
            return state.getBoard();
        }
        return ~state.getBoard();
    }

}
