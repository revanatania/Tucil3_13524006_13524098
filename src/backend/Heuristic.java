

public class Heuristic {

    public enum Type {
        MANHATTAN, EUCLIDEAN, CHEBYSHEV
    }

    public static double calculate(Type type, State state, Map map) {
        int nextCp = state.lastCheckpoint + 1;

        if (nextCp > map.maxCheckpoint) {
            return dist(type, state.x, state.y, map.goalX, map.goalY);
        }

        double total = 0.0;
        int curX = state.x;
        int curY = state.y;

        for (int cp = nextCp; cp <= map.maxCheckpoint; cp++) {
            int[] cpPos = map.getCheckpointPos(cp);
            if (cpPos == null) break; 
            total += dist(type, curX, curY, cpPos[0], cpPos[1]);
            curX = cpPos[0];
            curY = cpPos[1];
        }

        total += dist(type, curX, curY, map.goalX, map.goalY);

        return total;
    }

    private static double dist(Type type, int x1, int y1, int x2, int y2) {
        switch (type) {
            case MANHATTAN:
                return Math.abs(x1 - x2) + Math.abs(y1 - y2);
            case EUCLIDEAN:
                int dx = x1 - x2;
                int dy = y1 - y2;
                return Math.sqrt(dx * dx + dy * dy);
            case CHEBYSHEV:
                return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
            default:
                return 0;
        }
    }

    public static String getName(Type type) {
        switch (type) {
            case MANHATTAN:  return "Manhattan Distance";
            case EUCLIDEAN:  return "Euclidean Distance";
            case CHEBYSHEV:  return "Chebyshev Distance";
            default:         return "Unknown";
        }
    }
}