import java.util.Objects;

public class State {
    public int x;             
    public int y;             
    public int lastCheckpoint; 

    public State(int x, int y, int lastCheckpoint) {
        this.x = x;
        this.y = y;
        this.lastCheckpoint = lastCheckpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State s = (State) o;
        return x == s.x && y == s.y && lastCheckpoint == s.lastCheckpoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, lastCheckpoint);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", cp=" + lastCheckpoint + ")";
    }
}