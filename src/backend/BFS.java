import java.util.*;

public class BFS extends Solver {

    public BFS(Map map) {
        super(map);
    }

    @Override
    public SolverResult solve() {
        long startTime = System.nanoTime();
        int totalIterations = 0;

        Queue<int[]> frontier = new LinkedList<>();
        HashSet<State> visited = new HashSet<>();
        HashMap<State, int[]> parentMap = new HashMap<>();

        frontier.add(new int[]{map.startX, map.startY, -1, 0});

        while (!frontier.isEmpty()) {
            int[] current = frontier.poll();
            int x      = current[0];
            int y      = current[1];
            int lastCp = current[2];
            int g      = current[3];

            State currentState = new State(x, y, lastCp);

            if (visited.contains(currentState)) continue;
            visited.add(currentState);

            totalIterations++;

            if (isGoal(x, y, lastCp)) {
                SolverResult result = reconstructResult(parentMap, currentState,
                        totalIterations, startTime);
                result.executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
                return result;
            }

            for (int dir = 0; dir < 4; dir++) {
                SlideResult slide = slide(x, y, dir, lastCp);

                if (!slide.isValid()) continue;
                if (slide.newX == x && slide.newY == y) continue;

                State newState = new State(slide.newX, slide.newY, slide.newLastCheckpoint);
                if (visited.contains(newState)) continue;

                int newG = g + slide.moveCost;
                frontier.add(new int[]{slide.newX, slide.newY, slide.newLastCheckpoint, newG});

                if (!parentMap.containsKey(newState)) {
                    parentMap.put(newState, new int[]{
                            x, y, lastCp, DIR_CHAR[dir], newG
                    });
                }
            }
        }

        SolverResult result = new SolverResult();
        result.solutionFound = false;
        result.totalIterations = totalIterations;
        result.executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        return result;
    }
}