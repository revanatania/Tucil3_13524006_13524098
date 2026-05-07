import java.util.*;

public abstract class Solver {

    protected Map map;
    protected Heuristic.Type heuristicType; 

    protected static final int[] DX = {-1, 1, 0, 0};
    protected static final int[] DY = {0, 0, -1, 1};
    protected static final char[] DIR_CHAR = {'U', 'D', 'L', 'R'};

    public Solver(Map map) {
        this.map = map;
    }

    public Solver(Map map, Heuristic.Type heuristicType) {
        this.map = map;
        this.heuristicType = heuristicType;
    }

    public abstract SolverResult solve();
    
    protected SlideResult slide(int x, int y, int dirIdx, int lastCheckpoint) {
        int dx = DX[dirIdx];
        int dy = DY[dirIdx];

        int curX = x;
        int curY = y;
        int moveCost = 0;
        int tilesTraversed = 0;
        int newLastCheckpoint = lastCheckpoint;

        while (true) {
            int nextX = curX + dx;
            int nextY = curY + dy;

            if (nextX < 0 || nextX >= map.n || nextY < 0 || nextY >= map.m) {
                return new SlideResult(curX, curY, moveCost, tilesTraversed,
                        SlideResult.GAME_OVER_BOUNDS, newLastCheckpoint);
            }

            char nextTile = map.grid[nextX][nextY];

            if (nextTile == 'X') {
                if (curX == x && curY == y) {
                    return new SlideResult(x, y, 0, 0,
                            SlideResult.INVALID_MOVE, newLastCheckpoint);
                }
                return new SlideResult(curX, curY, moveCost, tilesTraversed,
                        SlideResult.OK, newLastCheckpoint);
            }

            if (nextTile == 'L') {
                return new SlideResult(nextX, nextY, moveCost + map.cost[nextX][nextY],
                        tilesTraversed + 1, SlideResult.GAME_OVER_LAVA, newLastCheckpoint);
            }

            if (Character.isDigit(nextTile)) {
                int cpNum = Character.getNumericValue(nextTile);

                if (cpNum == newLastCheckpoint + 1) {
                    newLastCheckpoint = cpNum;
                } else if (cpNum <= newLastCheckpoint) {
                } else {
                    return new SlideResult(nextX, nextY, moveCost + map.cost[nextX][nextY],
                            tilesTraversed + 1, SlideResult.GAME_OVER_WRONG_CP, newLastCheckpoint);
                }
            }

            curX = nextX;
            curY = nextY;
            moveCost += map.cost[curX][curY];
            tilesTraversed++;

            if (map.grid[curX][curY] == 'O') {
                if (newLastCheckpoint == map.maxCheckpoint) {
                    return new SlideResult(curX, curY, moveCost, tilesTraversed,
                            SlideResult.OK, newLastCheckpoint);
                }
            }
        }
    }

    protected boolean isGoal(int x, int y, int lastCheckpoint) {
        return x == map.goalX && y == map.goalY && lastCheckpoint == map.maxCheckpoint;
    }

    protected SolverResult reconstructResult(
            HashMap<State, int[]> parentMap,
            State goalState,
            int totalIterations,
            long startTime) {

        SolverResult result = new SolverResult();
        result.solutionFound = true;
        result.totalIterations = totalIterations;
        result.executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;

        List<Character> moves = new ArrayList<>();
        List<int[]> steps = new ArrayList<>();
        State current = goalState;

        while (parentMap.containsKey(current)) {
            int[] info = parentMap.get(current);
            moves.add(0, (char) info[3]);
            steps.add(0, new int[]{current.x, current.y, current.lastCheckpoint, info[4], info[3]});
            current = new State(info[0], info[1], info[2]);
        }

        result.moves = moves;
        result.steps = steps;
        result.totalCost = goalState != null && parentMap.containsKey(goalState)
                ? parentMap.get(goalState)[4]
                : 0;

        if (!steps.isEmpty()) {
            result.totalCost = steps.get(steps.size() - 1)[3];
        }

        return result;
    }
}