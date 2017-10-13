package game2048.mymcts;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class RandomSelector<T> implements Selector<T> {

    private final Random rng;

    public RandomSelector(Random rng) {
        this.rng = rng;
    }

    @Override
    public T select(List<T> items) {
        return select(rng, items);
    }

    public static <T> T select(Random rng, List<T> list) {
        return list.get(rng.nextInt(list.size()));
    }
}
