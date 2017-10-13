package game2048.mymcts;

import game2048.mcts.*;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class WeightedRandomSelector<T> implements Selector<T>{
    private final Random rng;

    public WeightedRandomSelector(Random rng) {
        this.rng = rng;
    }

    @Override
    public T select(List<T> items) {
        return selectWeighted(items);
    }
    
    public T selectWeighted(Iterable<T> list) {
        return selectWeighted(rng, list);
    }
    
    public static <T> T selectWeighted(Random rng, Iterable<T> list) {
        int sum = 0;
        T selected = null;
        for (T item : list) {
            int weight = item instanceof HasWeight? ((HasWeight)item).getWeight(): 1;
            sum += weight;
            if(rng.nextInt(sum) < weight) {
                selected = item;
            }
        }
        if(selected == null) {
            //either empty list or weight <= 0
            throw new AssertionError();
        }
        return selected;
    }
    
//    public <T extends HasWeight> T selectWeighted(Random rng, List<T> list) {
//        int sum = 0;
//        T selected = null;
//        for (T item : list) {
//            int weight = item.getWeight();
//            sum += weight;
//            if(rng.nextInt(sum) < weight) {
//                selected = item;
//            }
//        }
//        if(selected == null) {
//            //either empty list or weight <= 0
//            throw new AssertionError();
//        }
//        return selected;
//    }
}
