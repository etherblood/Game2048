package game2048;

/**
 *
 * @author Philipp
 */
public class SimpleLongState {
    private long board;
    private boolean playerTurn;

    public SimpleLongState() {
    }

    public SimpleLongState(SimpleLongState state) {
        copyFrom(state);
    }

    public long getBoard() {
        return board;
    }

    public void setBoard(long board) {
        this.board = board;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }
    
    public void flipPlayerTurn() {
        playerTurn = !playerTurn;
    }
    
    public void setSquare(int square, int value) {
        assert value == (value & 0xf);
        assert square == (square & 0xf);
        
        int index = Masks.SHIFT_COLUMN * square;
        board &= ~(Masks.MASK << index);
        board |= (long)value << index;
    }
    
    public int getSquare(int square) {
        assert square == (square & 0xf);
        
        return (int) ((board >>> (Masks.SHIFT_COLUMN * square)) & Masks.MASK);
    }
    
    public int getRow(int y) {
        assert y == (y & 3);
        
        return (int) ((board >>> (Masks.SHIFT_ROW * y)) & (Masks.MASK * Masks.ROW_0));
    }
    
    public void setRow(int y, int value) {
        assert y == (y & 3);
        assert value == (value & 0xffff);
        
        board &= ~((Masks.MASK * Masks.ROW_0) << (Masks.SHIFT_ROW * y));
        board |= (long)value << (Masks.SHIFT_ROW * y);
    }
    
    public int getColumn(int x) {
        assert x == (x & 3);
        
        return (int) (Masks.col0ToRow3(board >>> (x * Masks.SHIFT_COLUMN)) >>> (3 * Masks.SHIFT_ROW));
    }
    
    public int getColumnFlipped(int x) {
        assert x == (x & 3);
        
        return (int) (Masks.col0ToFlippedRow3(board >>> (x * Masks.SHIFT_COLUMN)) >>> (3 * Masks.SHIFT_ROW));
    }
    
    public void setColumn(int x, int value) {
        assert x == (x & 3);
        assert value == (value & 0xffff);
        
        long diagonal = (Masks.COLUMN_0 * value) & (Masks.MASK * Masks.DIAGONAL_0);
        long lastColumn = (diagonal * Masks.ROW_0) & ((Masks.MASK * Masks.COLUMN_0) << (3 * Masks.SHIFT_COLUMN));
        int shift = Masks.SHIFT_COLUMN * x;
        long column = lastColumn >>> (3 * Masks.SHIFT_COLUMN - shift);
        
        board &= ~((Masks.MASK * Masks.COLUMN_0) << shift);
        board |= column;
    }
    
    public void setColumnFlipped(int x, int value) {
        assert x == (x & 3);
        assert value == (value & 0xffff);
        
        long lastColumn = Masks.row0ToFlippedCol3(value);
        long column = lastColumn >>> Masks.SHIFT_COLUMN * (3 - x);
        
        board &= ~((Masks.COLUMN_0 * Masks.MASK) << Masks.SHIFT_COLUMN * x);
        board |= column;
    }
    
    public int squareIndex(int x, int y) {
        assert x == (x & 3);
        assert y == (y & 3);
        
        return (y << 2) | x;
    }
    
    public void copyFrom(SimpleLongState state) {
        board = state.board;
        playerTurn = state.playerTurn;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(board) ^ Boolean.hashCode(playerTurn);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof SimpleLongState)) {
            return false;
        }
        final SimpleLongState other = (SimpleLongState) obj;
        return this.board == other.board && this.playerTurn == other.playerTurn;
    }
}
