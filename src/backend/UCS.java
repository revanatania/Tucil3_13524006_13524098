import java.util.*;

public class UCS extends Solver {

    public UCS(Map map) {
        super(map);
    }

    @Override
    public SolverResult solve() {
        long startTime = System.currentTimeMillis();
        int totalIterations = 0;

        PriorityQueue<int[]> frontier = new PriorityQueue<>(
                Comparator.comparingInt(a -> a[0])
        );

        HashSet<State> visited = new HashSet<>();

        HashMap<State, int[]> parentMap = new HashMap<>();

        State startState = new State(map.startX, map.startY, -1);
        frontier.add(new int[]{0, map.startX, map.startY, -1});

        while (!frontier.isEmpty()) {
            int[] current = frontier.poll();
            int g    = current[0];
            int x    = current[1];
            int y    = current[2];
            int lastCp = current[3];

            State currentState = new State(x, y, lastCp);
            
            if (visited.contains(currentState)) continue;
            visited.add(currentState);

            totalIterations++;

            if (isGoal(x, y, lastCp)) {
                SolverResult result = reconstructResult(parentMap, currentState,
                        totalIterations, startTime);
                result.executionTimeMs = System.currentTimeMillis() - startTime;
                return result;
            }

            for (int dir = 0; dir < 4; dir++) {
                SlideResult slide = slide(x, y, dir, lastCp);

                if (!slide.isValid()) continue;

                if (slide.newX == x && slide.newY == y) continue;

                State newState = new State(slide.newX, slide.newY, slide.newLastCheckpoint);

                if (visited.contains(newState)) continue;

                int newG = g + slide.moveCost;

                frontier.add(new int[]{newG, slide.newX, slide.newY, slide.newLastCheckpoint});

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
        result.executionTimeMs = System.currentTimeMillis() - startTime;
        return result;
    }
}