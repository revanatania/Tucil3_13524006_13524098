
import java.util.List;

class BoardStateFrame {
    final int stepIndex;
    final int playerRow;
    final int playerCol;
    final String moveDirection;   
    final int costAtStep;
    final int lastCheckpoint;
    final List<int[]> slidePath;
    final List<int[]> visitedCells;

    BoardStateFrame(int stepIndex, int playerRow, int playerCol,
                    String moveDirection, int costAtStep, int lastCheckpoint,
                    List<int[]> slidePath, List<int[]> visitedCells) {
        this.stepIndex     = stepIndex;
        this.playerRow     = playerRow;
        this.playerCol     = playerCol;
        this.moveDirection = moveDirection;
        this.costAtStep    = costAtStep;
        this.lastCheckpoint = lastCheckpoint;
        this.slidePath     = slidePath;
        this.visitedCells  = visitedCells;
    }
}

class SolverRequest {
    enum Algorithm { UCS, GBFS, ASTAR }
    enum Heuristic { H1_MANHATTAN, H2_EUCLIDEAN, H3_CHEBYSHEV }

    final String boardFilePath;
    final Algorithm algorithm;
    final Heuristic heuristic;

    SolverRequest(String boardFilePath, Algorithm algorithm, Heuristic heuristic) {
        this.boardFilePath = boardFilePath;
        this.algorithm     = algorithm;
        this.heuristic     = heuristic;
    }
}

class SolverResultVM {
    String status;          
    String algorithmName;
    String heuristicName;
    String solutionPath;    
    int    totalCost;
    double executionTimeMs;
    int    iterationCount;
    List<BoardStateFrame> frames;
    int stepCount() { return frames == null ? 0 : frames.size(); }
}
