package game2048;

import java.io.PrintStream;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class LongStateController {
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    
    public static final int OVERFLOW = 0xffff;
    
    private static final int[] moveLeftMap, moveRightMap;//TODO: change to short?
    
    static {
        int[] flipValueMap = new int[1 << 16];
        for (int value = 0; value < flipValueMap.length; value++) {
            for (int i = 0; i < 4; i++) {
                int shift = 4 * i;
                flipValueMap[value] |= ((value >>> shift) & 0xf) << (12 - shift);
            }
            assert Integer.bitCount(flipValueMap[value]) == Integer.bitCount(value);
        }
        moveLeftMap = new int[1 << 16];
        for (int value = 0; value < moveLeftMap.length; value++) {
            int[] map = new int[4];
            for (int i = 0; i < 4; i++) {
                map[i] = (value >>> (4 * i)) & 0xf;
            }
            
            int leftMost = 0;
            for (int i = 1; i < 4; i++) {
                int current = map[i];
                if(current != 0) {
                    for (int j = i - 1; j >= leftMost; j--) {
                        if(map[j] == 0) {
                            map[j] = map[j + 1];
                            map[j + 1] = 0;
                        } else if(map[j] == map[j + 1]) {
                            leftMost = j + 1;
                            map[j]++;
                            map[j + 1] = 0;
                        }
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                if(map[i] >= 16) {
                    moveLeftMap[value] = OVERFLOW;
                    break;
                }
                moveLeftMap[value] |= map[i] << (4 * i);
            }
            assert moveLeftMap[value] == OVERFLOW || computeScore(value) == computeScore(moveLeftMap[value]): Integer.toHexString(value) + "->" + Integer.toHexString(moveLeftMap[value]);
        }
        moveRightMap = new int[1 << 16];
        for (int value = 0; value < moveRightMap.length; value++) {
            int movedLeft = moveLeftMap[value];
            int flippedValue = flipValueMap[value];
            moveRightMap[flippedValue] = movedLeft == OVERFLOW? OVERFLOW: flipValueMap[movedLeft];
            assert moveRightMap[flippedValue] == OVERFLOW || computeScore(flippedValue) == computeScore(moveRightMap[flippedValue]): Integer.toHexString(flippedValue) + "->" + Integer.toHexString(moveRightMap[flippedValue]);
        }
    }
    
    public static int computeScore(int value) {
        assert value == (value & 0xffff);
        
        int score = 0;
        for (int i = 0; i < 16; i+=4) {
            int squareValue = (value >>> i) & 0xf;
            if(squareValue != 0) {
                score += 1 << squareValue;
            }
        }
        return score;
    }
    
    public int score(LongState state) {
        int score = 0;
        for (int i = 0; i < 4; i++) {
            score += computeScore(state.getRow(i));
        }
        return score;
    }
    
    public void move(LongState state, int direction) {
        switch(direction) {
            case UP:
                int col0 = state.getColumnFlipped(0);
                int col1 = state.getColumnFlipped(1);
                int col2 = state.getColumnFlipped(2);
                int col3 = state.getColumnFlipped(3);
                
                col0 ^= moveDown(col0);
                col1 ^= moveDown(col1);
                col2 ^= moveDown(col2);
                col3 ^= moveDown(col3);
                
                long result = 0;
                result |= Masks.row0ToFlippedCol3(col0);
                result >>>= Masks.SHIFT_COLUMN;
                result |= Masks.row0ToFlippedCol3(col1);
                result >>>= Masks.SHIFT_COLUMN;
                result |= Masks.row0ToFlippedCol3(col2);
                result >>>= Masks.SHIFT_COLUMN;
                result |= Masks.row0ToFlippedCol3(col3);
                
                result ^= state.getState();
                state.setState(result);
//                for (int x = 0; x < 4; x++) {
//                    int column = state.getColumnFlipped(x);
////                    column = flip(column);
////                    column = moveDown(column);
////                    state.setColumnFlipped(x, column);
//                    column = moveDown(column);
//                    state.setColumnFlipped(x, column);
//                }
                break;
            case RIGHT:
                for (int y = 0; y < 4; y++) {
                    int row = state.getRow(y);
//                    row = flip(row);
//                    row = moveDown(row);
//                    row = flip(row);
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
    
    public boolean placeNewValue(LongState state, Random rng) {
        int square = randomEmptySquare(state, rng);
        if(square == -1) {
            return false;
        }
        int value = rng.nextInt(10) == 0? 2: 1;
        state.setValue(square, value);
        return true;
    }
    
    public void printState(LongState state, PrintStream out) {
        for (int y = 3; y >= 0; y--) {
            for (int x = 0; x < 4; x++) {
                int score = 1 << state.getValue(state.square(x, y));
                out.print(score + "\t");
            }
            out.println();
        }
    }
    
    public int randomEmptySquare(LongState state, Random rng) {
        long stateValue = state.getState();
        int count = 0;
        int current = -4;
        for (int i = 0; i < 64; i += 4) {
            long squareValue = (stateValue >>> i) & 0xf;
            if(squareValue == 0) {
                count++;
                if(rng.nextInt(count) == 0) {
                    current = i;
                }
            }
        }
        return current / 4;
    }
    
    public int countOccupiedSquares(LongState state) {
        return Long.bitCount(occupiedSquareFlags(state.getState()));
    }
    
    public boolean hasEmptySquare(LongState state) {
        return occupiedSquareFlags(state.getState()) != Masks.ALL_SQUARES;
    }

    private long occupiedSquareFlags(long mask) {
        mask |= mask >>> 2;
        mask &= 3 * Masks.ALL_SQUARES;
        mask |= mask >>> 1;
        mask &= Masks.ALL_SQUARES;
        return mask;
    }
    
//    protected int flip(int value) {
//        assert value == (value & 0xffff);
//        
//        return flipValueMap[value] & 0xffff;
//    }
    
    protected int moveDown(int value) {
        assert value == (value & 0xffff);
        
        int result = moveLeftMap[value];
        if(result == OVERFLOW) {
            throw new LongStateOverflowException();
        }
        return result;
    }
    
    protected int moveUp(int value) {
        assert value == (value & 0xffff);
        
        int result = moveRightMap[value];
        if(result == OVERFLOW) {
            throw new LongStateOverflowException();
        }
        return result;
    }
}
