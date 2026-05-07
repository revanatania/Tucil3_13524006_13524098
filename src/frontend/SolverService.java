import java.io.IOException;
import java.util.*;

public class SolverService {
    public SolverResultVM solve(SolverRequest request) {
        Map map;
        try {
            map = MapReader.readFromFile(request.boardFilePath);
        } catch (IOException e) {
            return error("Failed to read map: " + e.getMessage());
        }
        String validationError = map.validate();
        if (validationError != null) return error("Invalid map: " + validationError);

        Heuristic.Type hType = null;
        String hName = "—";
        if (request.algorithm == SolverRequest.Algorithm.GBFS ||
            request.algorithm == SolverRequest.Algorithm.ASTAR) {
            switch (request.heuristic) {
                case H1_MANHATTAN -> { hType = Heuristic.Type.MANHATTAN; hName = "Manhattan"; }
                case H2_EUCLIDEAN -> { hType = Heuristic.Type.EUCLIDEAN; hName = "Euclidean"; }
                case H3_CHEBYSHEV -> { hType = Heuristic.Type.CHEBYSHEV; hName = "Chebyshev"; }
            }
        }

        Solver solver = switch (request.algorithm) {
            case GBFS  -> new GBFS(map, hType);
            case ASTAR -> new AStar(map, hType);
            case BFS   -> new BFS(map);
            default    -> new UCS(map);
        };
        String algoName = switch (request.algorithm) {
            case UCS   -> "UCS";
            case GBFS  -> "GBFS";
            case ASTAR -> "A*";
            case BFS   -> "BFS";
        };

        SolverResult raw = solver.solve();
        SolverResultVM vm = new SolverResultVM();
        vm.algorithmName   = algoName;
        vm.heuristicName   = hName;
        vm.executionTimeMs = raw.executionTimeMs;
        vm.iterationCount  = raw.totalIterations;

        if (!raw.solutionFound) {
            vm.status       = "No Solution";
            vm.solutionPath = "—";
            vm.totalCost    = 0;
            vm.frames       = Collections.emptyList();
            return vm;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.moves.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(raw.moves.get(i));
        }

        vm.status       = "Solved";
        vm.solutionPath = sb.toString();
        vm.totalCost    = raw.totalCost;
        vm.frames       = buildFrames(raw, map);
        return vm;
    }

    public static Map loadMap(String filePath) throws IOException {
        return MapReader.readFromFile(filePath);
    }

    public static void save(String outPath, SolverResultVM vm,
                            List<BoardStateFrame> frames,
                            String filePath) throws IOException {
        Map map = MapReader.readFromFile(filePath);
        SolverResult raw = rebuildRaw(vm, frames);
        OutputWriter.saveToFile(outPath, raw, map, vm.algorithmName, vm.heuristicName);
    }

    private List<BoardStateFrame> buildFrames(SolverResult raw, Map map) {
        List<BoardStateFrame> frames = new ArrayList<>();
        frames.add(new BoardStateFrame(0, map.startX, map.startY,
                "START", 0, -1,
                List.of(new int[]{map.startX, map.startY}),
                Collections.emptyList()));

        int prevX = map.startX, prevY = map.startY;
        for (int i = 0; i < raw.steps.size(); i++) {
            int[] step  = raw.steps.get(i);
            int newX    = step[0], newY = step[1];
            int lastCp  = step[2], cost = step[3];
            char dir    = (char) step[4];
            List<int[]> slide = interpolate(prevX, prevY, newX, newY);
            frames.add(new BoardStateFrame(i + 1, newX, newY,
                    String.valueOf(dir), cost, lastCp, slide, Collections.emptyList()));
            prevX = newX; prevY = newY;
        }
        return frames;
    }

    private static List<int[]> interpolate(int r0, int c0, int r1, int c1) {
        List<int[]> path = new ArrayList<>();
        int dr = Integer.signum(r1 - r0), dc = Integer.signum(c1 - c0);
        int r = r0, c = c0;
        while (r != r1 || c != c1) { path.add(new int[]{r, c}); r += dr; c += dc; }
        path.add(new int[]{r1, c1});
        return path;
    }

    private static SolverResult rebuildRaw(SolverResultVM vm, List<BoardStateFrame> frames) {
        SolverResult raw = new SolverResult();
        raw.solutionFound   = true;
        raw.totalCost       = vm.totalCost;
        raw.totalIterations = vm.iterationCount;
        raw.executionTimeMs = vm.executionTimeMs;
        List<Character> moves = new ArrayList<>();
        List<int[]> steps = new ArrayList<>();
        for (int i = 1; i < frames.size(); i++) {
            BoardStateFrame f = frames.get(i);
            char dc = f.moveDirection.isEmpty() ? '?' : f.moveDirection.charAt(0);
            moves.add(dc);
            steps.add(new int[]{f.playerRow, f.playerCol, f.lastCheckpoint, f.costAtStep, dc});
        }
        raw.moves = moves;
        raw.steps = steps;
        return raw;
    }

    private SolverResultVM error(String msg) {
        SolverResultVM vm = new SolverResultVM();
        vm.status = "Error: " + msg;
        vm.algorithmName = "—"; vm.heuristicName = "—";
        vm.solutionPath = "—";
        vm.frames = Collections.emptyList();
        return vm;
    }
}