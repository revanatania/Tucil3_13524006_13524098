import java.util.List;

public class SolverResult {
    public boolean solutionFound;
    public List<Character> moves;     
    public int totalCost;             
    public int totalIterations;       
    public long executionTimeMs;        

    public List<int[]> steps;

    public SolverResult() {
        solutionFound = false;
        totalCost = 0;
        totalIterations = 0;
        executionTimeMs = 0;
    }

    public String getMovesString() {
        if (moves == null || moves.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : moves) sb.append(c);
        return sb.toString();
    }
}