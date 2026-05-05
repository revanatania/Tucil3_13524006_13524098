import java.util.ArrayList;
import java.util.List;

public class Map {
    public int n;
    public int m;
    public char[][] grid;
    public int[][] cost;
    public int maxCheckpoint;
    public int startX, startY;
    public int goalX, goalY;

    public Map(int n, int m) {
        this.n = n;
        this.m = m;
        this.grid = new char[n][m];
        this.cost = new int[n][m];
        this.maxCheckpoint = -1;
    }

    public void initialize() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                char c = grid[i][j];
                if (c == 'Z') {
                    startX = i;
                    startY = j;
                } else if (c == 'O') {
                    goalX = i;
                    goalY = j;
                } else if (Character.isDigit(c)) {
                    int cp = Character.getNumericValue(c);
                    if (cp > maxCheckpoint) {
                        maxCheckpoint = cp;
                    }
                }
            }
        }
    }

    public String validate() {
        boolean hasStart = false;
        boolean hasGoal = false;
        List<Integer> checkpoints = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                char c = grid[i][j];
                if (c == 'Z') {
                    if (hasStart) return "Map has more than one start tile (Z)";
                    hasStart = true;
                } else if (c == 'O') {
                    if (hasGoal) return "Map has more than one goal tile (O)";
                    hasGoal = true;
                } else if (Character.isDigit(c)) {
                    int cp = Character.getNumericValue(c);
                    if (checkpoints.contains(cp)) return "Duplicate checkpoint tile: " + cp;
                    checkpoints.add(cp);
                } else if (c != '*' && c != 'X' && c != 'L') {
                    return "Unknown tile '" + c + "' at row " + (i + 1) + " col " + (j + 1);
                }
            }
        }

        if (!hasStart) return "Map is missing start tile (Z)";
        if (!hasGoal)  return "Map is missing goal tile (O)";

        if (!checkpoints.isEmpty()) {
            checkpoints.sort(null);
            for (int i = 0; i < checkpoints.size(); i++) {
                if (checkpoints.get(i) != i) {
                    return "Checkpoint sequence is invalid: expected " + i + " but found " + checkpoints.get(i) + " (must be 0,1,2,...,n with no gaps)";
                }
            }
        }

        return null;
    }

    public int[] getCheckpointPos(int cpNum) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == (char) ('0' + cpNum)) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    public void print() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
    }

    public void printWithActor(int ax, int ay, int lastCp) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (i == ax && j == ay) {
                    System.out.print('Z');
                } else {
                    char c = grid[i][j];
                    if (Character.isDigit(c) && Character.getNumericValue(c) <= lastCp) {
                        System.out.print('*');
                    } else if (c == 'Z') {
                        System.out.print('*');
                    } else {
                        System.out.print(c);
                    }
                }
            }
            System.out.println();
        }
    }
}