package game2048;

/**
 *
 * @author Philipp
 */
public class Masks {

    public final static long COLUMN_0 = 0x0001000100010001L;
    public final static long ROW_0 = 0x1111L;
    public final static long ALL_SQUARES = ROW_0 * COLUMN_0;
    public final static long DIAGONAL_0 = 0x1000010000100001L;
    public final static long DIAGONAL_1 = 0x0001001001001000L;
    public final static long MASK = 0xfL;
    public final static int SHIFT_ROW = 16;
    public final static int SHIFT_COLUMN = 4;

    public static long col0ToRow3(long value) {
        return ((value & (COLUMN_0 * MASK)) * DIAGONAL_1) & ((ROW_0 * MASK) << (3 * SHIFT_ROW));
    }

    public static long col0ToFlippedRow3(long value) {
        return ((value & (COLUMN_0 * MASK)) * DIAGONAL_0) & ((ROW_0 * MASK) << (3 * SHIFT_ROW));
    }

    public static long row0ToCol3(long value) {
        long diagonal = ((value & (ROW_0 * MASK)) * COLUMN_0) & (DIAGONAL_0 * MASK);
        long result0 = (diagonal * ROW_0) & ((COLUMN_0 * MASK) << (3 * SHIFT_COLUMN));
        long a = value & 0xf;
        long d = (value >>> (3 * SHIFT_COLUMN)) & 0xf;
        if(a + d < 16) {
            long result1 = ((value & (ROW_0 * MASK)) * DIAGONAL_1) & ((COLUMN_0 * MASK) << (3 * SHIFT_COLUMN));
            assert result0 == result1;
        }
        return result0;
    }

    public static long row0ToFlippedCol3(long value) {
        return ((value & (ROW_0 * MASK)) * DIAGONAL_0) & ((COLUMN_0 * MASK) << (3 * SHIFT_COLUMN));
    }

    public static long rotateCounterClockwise(long value) {
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result >>>= SHIFT_ROW;
            result |= col0ToFlippedRow3(value);
            value >>>= SHIFT_COLUMN;
        }
        return result;
    }

    public static long flipHorizontally(long value) {
        return ((value & ((COLUMN_0 * MASK) << (0 * SHIFT_COLUMN))) << (3 * SHIFT_COLUMN))
                | ((value & ((COLUMN_0 * MASK) << (1 * SHIFT_COLUMN))) << (1 * SHIFT_COLUMN))
                | ((value & ((COLUMN_0 * MASK) << (2 * SHIFT_COLUMN))) >>> (1 * SHIFT_COLUMN))
                | ((value & ((COLUMN_0 * MASK) << (3 * SHIFT_COLUMN))) >>> (3 * SHIFT_COLUMN));
    }

    public static long flipVertically(long value) {
        return ((value & ((ROW_0 * MASK) << (0 * SHIFT_ROW))) << (3 * SHIFT_ROW))
                | ((value & ((ROW_0 * MASK) << (1 * SHIFT_ROW))) << (1 * SHIFT_ROW))
                | ((value & ((ROW_0 * MASK) << (2 * SHIFT_ROW))) >>> (1 * SHIFT_ROW))
                | ((value & ((ROW_0 * MASK) << (3 * SHIFT_ROW))) >>> (3 * SHIFT_ROW));
    }
}
