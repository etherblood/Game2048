package game2048;

/**
 *
 * @author Philipp
 */
public class LongState {

    private long state;

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }

    public void setValue(int square, int value) {
        assert value == (value & 0xf);
        assert square == (square & 0xf);

        int index = 4 * square;
        state &= ~(0xfL << index);
        state |= (long) value << index;
    }

    public int getValue(int square) {
        assert square == (square & 0xf);

        return (int) ((state >>> (4 * square)) & 0xf);
    }

    public int getRow(int y) {
        assert y == (y & 3);

        return (int) ((state >>> (16 * y)) & 0xffff);
    }

    public void setRow(int y, int value) {
        assert y == (y & 3);
        assert value == (value & 0xffff);

        state &= ~(0xffffL << (16 * y));
        state |= (long) value << (16 * y);
    }

    public int getColumn(int x) {
        assert x == (x & 3);

        return (int) Masks.col0ToRow0(state >>> (x * Masks.SHIFT_COLUMN));
    }

    public int getColumnFlipped(int x) {
        assert x == (x & 3);

        return (int) Masks.col0ToFlippedRow0(state >>> (x * Masks.SHIFT_COLUMN));
    }

    public void setColumn(int x, int value) {
        assert x == (x & 3);
        assert value == (value & 0xffff);

        long diagonal = (0x1000100010001L * value) & 0xf0000f0000f0000fL;
        long lastColumn = (diagonal * 0x1111L) & 0xf000f000f000f000L;
        int shift = 4 * x;
        long column = lastColumn >>> (12 - shift);

        state &= ~(0xf000f000f000fL << shift);
        state |= column;
    }

    public void setColumnFlipped(int x, int value) {
        assert x == (x & 3);
        assert value == (value & 0xffff);

        long lastColumn = Masks.row0ToFlippedCol3(value);
        long column = lastColumn >>> Masks.SHIFT_COLUMN * (3 - x);

        state &= ~((Masks.COLUMN_0 * Masks.MASK) << (x * Masks.SHIFT_COLUMN));
        state |= column;
    }

    public int square(int x, int y) {
        assert x == (x & 3);
        assert y == (y & 3);

        return (y << 2) | x;
    }
}
