
import java.util.*;

public class AStar extends Solver {

    public AStar(Map map, Heuristic.Type heuristicType) {
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
        double f0 = 0 + h0;
        frontier.add(new long[]{(long)(f0 * 1000), 0, map.startX, map.startY, -1});

        while (!frontier.isEmpty()) {
            long[] current = frontier.poll();
            int g      = (int) current[1];
            int x      = (int) current[2];
            int y      = (int) current[3];
            int lastCp = (int) current[4];

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
                double h = Heuristic.calculate(heuristicType, newState, map);
                double f = newG + h;

                frontier.add(new long[]{(long)(f * 1000), newG,
                        slide.newX, slide.newY, slide.newLastCheckpoint});

                if (!parentMap.containsKey(newState) ||
                        parentMap.get(newState)[4] > newG) {
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