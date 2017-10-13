package game2048.mymcts;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Philipp
 */
public class MctsNode<M> {
    private Map<M, MctsNode<M>> childs;
    private Map<M, Double> childVisits;
    private double totalVisits, totalWins;
    
    public boolean isInitialized() {
        return childs != null;
    }
    
    public void addWins(double wins) {
        totalWins += wins;
    }
    
    public void addVisits(double visits) {
        totalVisits += visits;
    }
    
    public MctsNode<M> getChild(M move) {
        return childs.get(move);
    }
    
    public void setChild(M move, MctsNode<M> child) {
        childs.put(move, child);
    }

    public double getAverageWins() {
        return totalWins / totalVisits;
    }
    
    public double getTotalVisits() {
        return totalVisits;
    }

    public double getTotalWins() {
        return totalWins;
    }
    
    public double getChildVisits(M move) {
        return childVisits.getOrDefault(move, 0d);
    }
    
    public void increaseChildVisits(M move, double visits) {
        childVisits.put(move, getChildVisits(move) + visits);
    }
    
    public Collection<MctsNode<M>> getChilds() {
        return childs.values();
    }

    public void init(Map<M, MctsNode<M>> childs, Map<M, Double> childVisits) {
        assert this.childs == null;
        this.childs = childs;
        this.childVisits = childVisits;
    }
}
