import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SolverView {

    private final BorderPane root;
    private final MainApp    app;
    private ControlPanel  controlPanel;
    private BoardGridView boardGrid;
    private ResultPanel   resultPanel;
    private BottomPanel   bottomPanel;
    private Label         zoomLabel;
    private SolverResultVM        currentResult;
    private List<BoardStateFrame> frames   = new ArrayList<>();
    private int                   curFrame = 0;
    private Timeline              timeline;
    private double                speed    = 1.0;
    private char[][] grid;
    private int      startX, startY;
    private String   filePath;

    public SolverView(MainApp app) {
        this.app = app;
        root = new BorderPane();
        root.getStyleClass().add("solver-root");
        buildLayout();
    }

    public Pane getRoot() { return root; }
    private void buildLayout() {
        
        controlPanel = new ControlPanel();
        controlPanel.setOnRun(this::runSolver);
        controlPanel.setOnReset(this::reset);
        controlPanel.setOnFileLoaded(this::loadFile);
        controlPanel.setOnSave(this::saveSolution);
        controlPanel.setOnSaveLog(this::saveLog);
        root.setLeft(controlPanel);

        boardGrid = new BoardGridView();
        ScrollPane boardScroll = new ScrollPane(boardGrid);
        boardScroll.getStyleClass().add("board-scroll");
        boardScroll.setFitToWidth(false);
        boardScroll.setFitToHeight(false);  
        boardScroll.setPannable(true);
        boardScroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) ->
            boardGrid.setViewportSize(newVal.getWidth() - 8, newVal.getHeight() - 8)
        );

        HBox zoomBar = buildZoomBar();
        VBox centerWrap = new VBox(8, zoomBar, boardScroll);
        centerWrap.getStyleClass().add("board-wrapper");
        centerWrap.setPadding(new Insets(12));
        VBox.setVgrow(boardScroll, Priority.ALWAYS);
        root.setCenter(centerWrap);

        resultPanel = new ResultPanel();
        root.setRight(resultPanel);

        bottomPanel = new BottomPanel();
        bottomPanel.setOnFirst(() -> seek(0));
        bottomPanel.setOnPrev (() -> seek(curFrame - 1));
        bottomPanel.setOnPlay (this::togglePlay);
        bottomPanel.setOnNext (() -> seek(curFrame + 1));
        bottomPanel.setOnLast (() -> seek(frames.size() - 1));
        bottomPanel.setOnSeek (this::seek);
        bottomPanel.setOnSpeedChange(s -> { speed = s; if (isPlaying()) startPlay(); });
        root.setBottom(bottomPanel);
    }

    private HBox buildZoomBar() {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("zoom-toolbar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Board Zoom");
        title.getStyleClass().add("zoom-title");

        Button zoomOut = new Button("-");
        zoomOut.getStyleClass().addAll("btn-secondary", "zoom-btn");
        zoomOut.setOnAction(e -> {
            boardGrid.zoomOut();
            updateZoomLabel();
        });

        Button zoomIn = new Button("+");
        zoomIn.getStyleClass().addAll("btn-secondary", "zoom-btn");
        zoomIn.setOnAction(e -> {
            boardGrid.zoomIn();
            updateZoomLabel();
        });

        Button fitBtn = new Button("Fit");
        fitBtn.getStyleClass().addAll("btn-secondary", "zoom-fit-btn");
        fitBtn.setOnAction(e -> {
            boardGrid.resetZoom();
            updateZoomLabel();
        });

        zoomLabel = new Label("100%");
        zoomLabel.getStyleClass().add("zoom-value");

        bar.getChildren().addAll(title, zoomOut, zoomIn, fitBtn, zoomLabel);
        return bar;
    }

    private void updateZoomLabel() {
        if (zoomLabel != null) {
            zoomLabel.setText(boardGrid.getZoomPercent() + "%");
        }
    }

    private void loadFile(String path) {
        try {
            Map map = SolverService.loadMap(path);
            String err = map.validate();
            if (err != null) { alert("Invalid Map", err); return; }
            filePath = path;
            grid     = map.grid;
            startX   = map.startX;
            startY   = map.startY;
            boardGrid.resetZoom();
            boardGrid.loadBoard(translate(grid, startX, startY, -1));
            updateZoomLabel();
            clearState();
        } catch (IOException e) {
            alert("Error", e.getMessage());
        }
    }

    private char[][] translate(char[][] src, int px, int py, int lastCp) {
        int n = src.length, m = src[0].length;
        char[][] out = new char[n][m];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                if (r == px && c == py) { out[r][c] = 'S'; continue; }
                char ch = src[r][c];
                out[r][c] = switch (ch) {
                    case 'Z' -> '*';
                    case 'O' -> 'G';
                    default  -> (Character.isDigit(ch) && Character.getNumericValue(ch) <= lastCp)
                                ? '*' : ch;
                };
            }
        }
        return out;
    }

    private void runSolver() {
        if (filePath == null) { alert("No File", "Please load a .txt board file first."); return; }
        stopPlay();
        controlPanel.setRunning(true);

        SolverRequest.Algorithm algo = controlPanel.getAlgorithm();
        SolverRequest.Heuristic heur = controlPanel.getHeuristic();

        SolverRequest req     = new SolverRequest(filePath, algo, heur);
        SolverService service = new SolverService();

        Task<SolverResultVM> task = new Task<>() {
            @Override protected SolverResultVM call() { return service.solve(req); }
        };
        task.setOnSucceeded(e -> {
            currentResult = task.getValue();
            frames = currentResult.frames != null ? currentResult.frames : new ArrayList<>();
            controlPanel.setRunning(false);
            controlPanel.setSolvedState("Solved".equals(currentResult.status));
            resultPanel.showResult(currentResult);
            bottomPanel.setTotalSteps(Math.max(0, frames.size() - 1));
            
            bottomPanel.setLogEntries(buildLogRows(frames));
            seek(0);
        });
        task.setOnFailed(e -> {
            controlPanel.setRunning(false);
            alert("Solver Error", task.getException().getMessage());
        });
        new Thread(task, "solver-thread").start();
    }

    
    private List<BottomPanel.LogRow> buildLogRows(List<BoardStateFrame> frames) {
        List<BottomPanel.LogRow> rows = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            BoardStateFrame f = frames.get(i);
            String pos  = "(" + f.playerRow + "," + f.playerCol + ")";
            String move = i == 0 ? "START" : f.moveDirection;
            String cp   = f.lastCheckpoint < 0 ? "\u2014" : String.valueOf(f.lastCheckpoint);
            String stat = (i == frames.size() - 1 && "Solved".equals(
                currentResult != null ? currentResult.status : "")) ? "goal" : "expanded";
            rows.add(new BottomPanel.LogRow(i, pos, move, f.costAtStep, 0.0, cp, stat));
        }
        return rows;
    }

    private void seek(int target) {
        if (grid == null) return;
        if (frames.isEmpty()) {
            boardGrid.loadBoard(translate(grid, startX, startY, -1));
            bottomPanel.setCurrentStep(0);
            return;
        }
        curFrame = Math.max(0, Math.min(frames.size() - 1, target));
        BoardStateFrame f = frames.get(curFrame);

        List<int[]> path = new ArrayList<>();
        for (int i = 1; i <= curFrame; i++) {
            List<int[]> sp = frames.get(i).slidePath;
            if (sp != null) path.addAll(sp);
        }

        boardGrid.loadBoard(translate(grid, f.playerRow, f.playerCol, f.lastCheckpoint));
        boardGrid.applyOverlay(f.playerRow, f.playerCol, path, f.visitedCells);
        resultPanel.updateStep(f);
        bottomPanel.setCurrentStep(curFrame);
        bottomPanel.highlightLogRow(curFrame);
    }

    private void togglePlay() {
        if (isPlaying()) { stopPlay(); bottomPanel.setPlayingState(false); }
        else             { startPlay(); bottomPanel.setPlayingState(true); }
    }

    private void startPlay() {
        stopPlay();
        timeline = new Timeline(new KeyFrame(Duration.millis(800 / speed), e -> {
            if (curFrame < frames.size() - 1) {
                seek(curFrame + 1);
            } else {
                stopPlay();
                bottomPanel.setPlayingState(false);
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void stopPlay() {
        if (timeline != null) { timeline.stop(); timeline = null; }
    }

    private boolean isPlaying() {
        return timeline != null && timeline.getStatus() == Animation.Status.RUNNING;
    }

    private void saveSolution() {
        if (currentResult == null || !"Solved".equals(currentResult.status)) {
            alert("Nothing to Save", "Run the solver first."); return;
        }
        File f = savePicker("Save Solution", "solution.txt");
        if (f == null) return;
        try {
            SolverService.save(f.getAbsolutePath(), currentResult, frames, filePath);
            info("Saved", "Solution saved to:\n" + f.getAbsolutePath());
        } catch (IOException e) { alert("Save Failed", e.getMessage()); }
    }

    private void saveLog() {
        if (currentResult == null) { alert("No Result", "Run the solver first."); return; }
        File f = savePicker("Save Search Log", "search_log.txt");
        if (f == null) return;
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(f));
            pw.println("=== IcePath Solver — Search Log ===");
            pw.println("Algorithm : " + currentResult.algorithmName);
            pw.println("Heuristic : " + currentResult.heuristicName);
            pw.println("Status    : " + currentResult.status);
            pw.println("Iterations: " + currentResult.iterationCount);
            pw.println("Time      : " + currentResult.executionTimeMs + " ms");
            pw.println();
            pw.printf("%-6s %-12s %-6s %-8s %-8s %-6s%n",
                "#", "Position", "Move", "Cost", "Checkpoint", "Status");
            pw.println("-".repeat(54));
            for (int i = 0; i < frames.size(); i++) {
                BoardStateFrame fr = frames.get(i);
                String move = i == 0 ? "START" : fr.moveDirection;
                String cp   = fr.lastCheckpoint < 0 ? "-" : String.valueOf(fr.lastCheckpoint);
                String stat = (i == frames.size() - 1 && "Solved".equals(currentResult.status))
                    ? "goal" : "step";
                pw.printf("%-6d (%d,%-3d)   %-6s %-8d %-6s %-6s%n",
                    i, fr.playerRow, fr.playerCol, move, fr.costAtStep, cp, stat);
            }
            pw.close();
            info("Saved", "Log saved to:\n" + f.getAbsolutePath());
        } catch (IOException e) { alert("Save Failed", e.getMessage()); }
    }

    private void reset() {
        stopPlay();
        clearState();
        if (grid != null) {
            boardGrid.resetZoom();
            boardGrid.loadBoard(translate(grid, startX, startY, -1));
            updateZoomLabel();
        }
    }

    private void clearState() {
        currentResult = null;
        frames        = new ArrayList<>();
        curFrame      = 0;
        stopPlay();
        resultPanel.reset();
        controlPanel.setSolvedState(false);
        bottomPanel.setTotalSteps(0);
        bottomPanel.setCurrentStep(0);
        bottomPanel.setPlayingState(false);
        bottomPanel.clearLog();
    }

    private File savePicker(String title, String initial) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialFileName(initial);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        return fc.showSaveDialog(root.getScene() != null ? root.getScene().getWindow() : null);
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
