
import java.util.*;

public class GBFS extends Solver {

    public GBFS(Map map, Heuristic.Type heuristicType) {
        super(map, heuristicType);
    }

    @Override
    public SolverResult solve() {
        long startTime = System.nanoTime();
        int totalIterations = 0;

        PriorityQueue<long[]> frontier = new PriorityQueue<>(
                Comparator.comparingLong(a -> a[0])
        );

        HashSet<State> visited = new HashSet<>();

        HashMap<State, int[]> parentMap = new HashMap<>();

        State startState = new State(map.startX, map.startY, -1);
        double h0 = Heuristic.calculate(heuristicType, startState, map);
        frontier.add(new long[]{(long)(h0 * 1000), map.startX, map.startY, -1, 0});

        while (!frontier.isEmpty()) {
            long[] current = frontier.poll();
            int x      = (int) current[1];
            int y      = (int) current[2];
            int lastCp = (int) current[3];
            int g      = (int) current[4];

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

                double h = Heuristic.calculate(heuristicType, newState, map);
                int newG = g + slide.moveCost;

                frontier.add(new long[]{(long)(h * 1000), slide.newX, slide.newY,
                        slide.newLastCheckpoint, newG});

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