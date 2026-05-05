import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Map map = null;
        while (map == null) {
            System.out.print(">> Enter input file path: ");
            String filePath = scanner.nextLine().trim();

            if (filePath.isEmpty()) {
                System.out.println("Error: File path cannot be empty.");
                continue;
            }

            try {
                map = MapReader.readFromFile(filePath);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.print("Try again? (yes/no): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("yes") && !retry.equals("y")) return;
                continue;
            }

            String validationError = map.validate();
            if (validationError != null) {
                System.out.println("Invalid map: " + validationError);
                System.out.print("Try again? (yes/no): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("yes") && !retry.equals("y")) return;
                map = null;
            }
        }

        System.out.println("Map loaded successfully (" + map.n + "x" + map.m + ")");
        System.out.println();
        map.print();
        System.out.println();

        String algoInput = null;
        while (algoInput == null) {
            System.out.print(">> Choose algorithm (UCS/GBFS/A*): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("UCS") || input.equals("GBFS") || input.equals("A*")) {
                algoInput = input;
            } else {
                System.out.println("Error: Invalid algorithm '" + input + "'. Must be UCS, GBFS, or A*.");
            }
        }

        boolean needsHeuristic = algoInput.equals("GBFS") || algoInput.equals("A*");
        Heuristic.Type heuristicType = null;
        String heuristicName = null;

        if (needsHeuristic) {
            System.out.println(">> Choose heuristic:");
            System.out.println("   H1 - Manhattan Distance");
            System.out.println("   H2 - Euclidean Distance");
            System.out.println("   H3 - Chebyshev Distance");
            while (heuristicType == null) {
                System.out.print(">> Your choice (H1/H2/H3): ");
                String hInput = scanner.nextLine().trim().toUpperCase();
                switch (hInput) {
                    case "H1": heuristicType = Heuristic.Type.MANHATTAN; break;
                    case "H2": heuristicType = Heuristic.Type.EUCLIDEAN; break;
                    case "H3": heuristicType = Heuristic.Type.CHEBYSHEV; break;
                    default: System.out.println("Error: Invalid heuristic '" + hInput + "'. Must be H1, H2, or H3.");
                }
            }
            heuristicName = Heuristic.getName(heuristicType);
        }

        System.out.println();
        System.out.println("Solving...");

        Solver solver;
        switch (algoInput) {
            case "GBFS": solver = new GBFS(map, heuristicType); break;
            case "A*":   solver = new AStar(map, heuristicType); break;
            default:     solver = new UCS(map);
        }

        SolverResult result = solver.solve();

        System.out.println();
        if (!result.solutionFound) {
            System.out.println("No solution found.");
        } else {
            System.out.println("Solution Found : " + result.getMovesString());
            System.out.println("Cost           : " + result.totalCost);
            System.out.println();

            System.out.println("Initial:");
            map.printWithActor(map.startX, map.startY, -1);
            System.out.println();

            for (int i = 0; i < result.steps.size(); i++) {
                int[] step = result.steps.get(i);
                System.out.println("Step " + (i + 1) + " : " + (char) step[4]);
                map.printWithActor(step[0], step[1], step[2]);
                System.out.println();
            }
        }

        System.out.println(">> Execution time : " + result.executionTimeMs + " ms");
        System.out.println(">> Iterations     : " + result.totalIterations);
        System.out.println();

        if (result.solutionFound) {
            System.out.print(">> Perform playback? (yes/no): ");
            String playbackInput = scanner.nextLine().trim().toLowerCase();
            if (playbackInput.equals("yes") || playbackInput.equals("y")) {
                runPlayback(scanner, result, map);
            }

            System.out.print(">> Save solution to file? (yes/no): ");
            String saveInput = scanner.nextLine().trim().toLowerCase();
            if (saveInput.equals("yes") || saveInput.equals("y")) {
                boolean saved = false;
                while (!saved) {
                    System.out.print(">> Enter output file path: ");
                    String outPath = scanner.nextLine().trim();
                    if (outPath.isEmpty()) {
                        System.out.println("Error: Output path cannot be empty.");
                        continue;
                    }
                    try {
                        OutputWriter.saveToFile(outPath, result, map, algoInput, heuristicName);
                        System.out.println(">> Solution saved to: " + outPath);
                        saved = true;
                    } catch (IOException e) {
                        System.out.println("Error saving file: " + e.getMessage());
                        System.out.print("Try again? (yes/no): ");
                        String retry = scanner.nextLine().trim().toLowerCase();
                        if (!retry.equals("yes") && !retry.equals("y")) saved = true;
                    }
                }
            }
        }

        scanner.close();
    }

    private static void runPlayback(Scanner scanner, SolverResult result, Map map) {
        int totalSteps = result.steps.size();
        int currentStep = 0;

        System.out.println();
        System.out.println("=== PLAYBACK ===");
        System.out.println("Commands: [n] next, [p] prev, [j] jump to step, [q] quit");
        System.out.println("Total steps: " + totalSteps);
        System.out.println();

        printPlaybackStep(map, result, currentStep);

        while (true) {
            System.out.print(">> Command: ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            switch (cmd) {
                case "n":
                    if (currentStep < totalSteps) {
                        currentStep++;
                    } else {
                        System.out.println("Already at last step.");
                    }
                    break;
                case "p":
                    if (currentStep > 0) {
                        currentStep--;
                    } else {
                        System.out.println("Already at initial state.");
                    }
                    break;
                case "j":
                    System.out.print(">> Jump to step (0 = initial, " + totalSteps + " = final): ");
                    String jumpInput = scanner.nextLine().trim();
                    try {
                        int target = Integer.parseInt(jumpInput);
                        if (target >= 0 && target <= totalSteps) {
                            currentStep = target;
                        } else {
                            System.out.println("Error: Step must be between 0 and " + totalSteps + ".");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: '" + jumpInput + "' is not a valid step number.");
                    }
                    break;
                case "q":
                    System.out.println("Exiting playback.");
                    return;
                default:
                    System.out.println("Unknown command '" + cmd + "'. Use: n, p, j, q");
            }

            printPlaybackStep(map, result, currentStep);
        }
    }

    private static void printPlaybackStep(Map map, SolverResult result, int step) {
        System.out.println();
        if (step == 0) {
            System.out.println("--- Initial State ---");
            map.printWithActor(map.startX, map.startY, -1);
        } else {
            int[] s = result.steps.get(step - 1);
            System.out.println("--- Step " + step + " : " + (char) s[4] + " ---");
            map.printWithActor(s[0], s[1], s[2]);
            System.out.println("Cost so far: " + s[3]);
        }
        System.out.println("Step " + step + " / " + result.steps.size());
        System.out.println();
    }
}