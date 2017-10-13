package game2048.mcts;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public class RandomSelector {
    public <T> T select(Random rng, List<T> list) {
        return list.get(rng.nextInt(list.size()));
    }
    
    public <T> T selectWeighted(Random rng, List<T> list) {
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
