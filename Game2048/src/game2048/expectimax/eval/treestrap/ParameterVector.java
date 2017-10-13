package game2048.expectimax.eval.treestrap;

import game2048.expectimax.eval.TrainedValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Philipp
 */
public class ParameterVector<K> {
    private final Map<K, Float> map = new HashMap<>();
    
    public void multLocal(float scale) {
        for (K key : map.keySet()) {
            map.put(key, scale * map.get(key));
        }
    }
    
    public void addLocal(ParameterVector<K> vector) {
        for (Map.Entry<K, Float> entry : vector.map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }
    
    public void add(K key, float value) {
        assert Float.isFinite(value);
        float old = map.getOrDefault(key, 0f);
        map.put(key, old + value);
    }
    
    public Set<Entry<K, Float>> entrySet() {
        return map.entrySet();
    }
}
