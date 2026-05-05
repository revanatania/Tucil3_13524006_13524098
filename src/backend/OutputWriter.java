import java.io.*;

public class OutputWriter {

    public static void saveToFile(String filePath, SolverResult result, Map map,
                                   String algorithm, String heuristic) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(filePath));

        pw.println("=== Ice Sliding Puzzle Solution ===");
        pw.println("Algorithm  : " + algorithm);
        if (heuristic != null) {
            pw.println("Heuristic  : " + heuristic);
        }
        pw.println();

        if (!result.solutionFound) {
            pw.println("No solution found.");
        } else {
            pw.println("Solution   : " + result.getMovesString());
            pw.println("Cost       : " + result.totalCost);
            pw.println("Iterations : " + result.totalIterations);
            pw.println("Time       : " + result.executionTimeMs + " ms");
            pw.println();

            pw.println("Initial:");
            printMapToWriter(pw, map, map.startX, map.startY, -1);
            pw.println();

            for (int i = 0; i < result.steps.size(); i++) {
                int[] step = result.steps.get(i);
                int x = step[0], y = step[1], lastCp = step[2];
                char dir = (char) step[4];
                pw.println("Step " + (i + 1) + " : " + dir);
                printMapToWriter(pw, map, x, y, lastCp);
                pw.println();
            }
        }

        pw.close();
    }

    private static void printMapToWriter(PrintWriter pw, Map map, int ax, int ay, int lastCp) {
        for (int i = 0; i < map.n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < map.m; j++) {
                if (i == ax && j == ay) {
                    sb.append('Z');
                } else {
                    char c = map.grid[i][j];
                    if (Character.isDigit(c) && Character.getNumericValue(c) <= lastCp) {
                        sb.append('*');
                    } else if (c == 'Z') {
                        sb.append('*');
                    } else {
                        sb.append(c);
                    }
                }
            }
            pw.println(sb.toString());
        }
    }
}