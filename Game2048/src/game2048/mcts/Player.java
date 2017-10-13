package game2048.mcts;

/**
 *
 * @author Philipp
 */
public class Player {
    private final int id;

    public Player(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return 41 * 7 + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Player)) {
            return false;
        }
        Player other = (Player) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "Player{" + "id=" + id + '}';
    }

}
