package game2048.mymcts;

import java.util.List;

/**
 *
 * @author Philipp
 */
public interface Selector<T> {
    T select(List<T> items);
}
