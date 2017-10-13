package game2048;

import game2048.mcts.HasWeight;
import game2048.mcts.Move;

/**
 *
 * @author Philipp
 */
public class SetSquareValueMove implements Move, HasWeight{
    private final int square, value, weight;

    public SetSquareValueMove(int square, int value, int weight) {
        assert 0 <= square && square < 16;
        this.square = square;
        this.value = value;
        this.weight = weight;
    }

    public int getSquare() {
        return square;
    }

    public int getValue() {
        return value;
    }
    
    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.square;
        hash = 41 * hash + this.value;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SetSquareValueMove)) {
            return false;
        }
        SetSquareValueMove other = (SetSquareValueMove) obj;
        return this.square == other.square && this.value == other.value;
    }

    @Override
    public String toString() {
        return "SetSquareValueMove{square=" + square + ", value=" + value + ", weight=" + weight + '}';
    }

}
