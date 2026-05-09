import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.Consumer;

public class ControlPanel extends ScrollPane {

    private TextField        fileField;
    private ComboBox<String> algoCombo;
    private ComboBox<String> heurCombo;
    private Button           runBtn, resetBtn, saveBtn, saveLogBtn;
    private Label            statusLbl;

    private Runnable         onRun, onReset, onSave, onSaveLog;
    private Consumer<String> onFileLoaded;

    public ControlPanel() {
        getStyleClass().add("control-panel-scroll");
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setPrefWidth(250);
        setMinWidth(250);
        setMaxWidth(250);

        VBox inner = new VBox(14);
        inner.getStyleClass().add("control-panel");
        inner.setPadding(new Insets(16));
        inner.getChildren().addAll(
            sectionTitle("CONTROLS"),
            fileSection(),
            algoSection(),
            heurSection(),
            buttonSection(),
            legend()
        );

        setContent(inner);
    }

    private VBox fileSection() {
        VBox box = vbox(6);
        box.getChildren().add(ctrlLabel("Input File"));

        fileField = new TextField("No file selected");
        fileField.getStyleClass().add("ctrl-input");
        fileField.setEditable(false);

        Button browse = new Button("\uD83D\uDCC2  Browse File");
        browse.getStyleClass().addAll("btn-secondary", "btn-full");
        browse.setOnAction(e -> pickFile());

        statusLbl = new Label();
        statusLbl.getStyleClass().add("status-label");

        box.getChildren().addAll(fileField, browse, statusLbl);
        return box;
    }

    private VBox algoSection() {
        VBox box = vbox(6);
        box.getChildren().add(ctrlLabel("Algorithm"));

        algoCombo = new ComboBox<>();
        algoCombo.getItems().addAll(
            "A*  \u2014  A-Star",
            "UCS  \u2014  Uniform Cost Search",
            "GBFS  \u2014  Greedy Best First Search",
            "BFS  \u2014  Breadth First Search"
        );
        algoCombo.setValue("A*  \u2014  A-Star");
        algoCombo.getStyleClass().add("ctrl-combo");
        algoCombo.setMaxWidth(Double.MAX_VALUE);
        algoCombo.setOnAction(e -> {
            String val = algoCombo.getValue();
            boolean noHeur = val.startsWith("UCS") || val.startsWith("BFS");
            heurCombo.setDisable(noHeur);
        });

        box.getChildren().add(algoCombo);
        return box;
    }

    private VBox heurSection() {
        VBox box = vbox(6);
        box.getChildren().add(ctrlLabel("Heuristic"));

        heurCombo = new ComboBox<>();
        heurCombo.getItems().addAll(
            "H1  \u2014  Manhattan",
            "H2  \u2014  Euclidean",
            "H3  \u2014  Chebyshev"
        );
        heurCombo.setValue("H1  \u2014  Manhattan");
        heurCombo.getStyleClass().add("ctrl-combo");
        heurCombo.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().add(heurCombo);
        return box;
    }

    private VBox buttonSection() {
        VBox box = vbox(8);

        runBtn = new Button("\uD83D\uDD0D  Run Solver");
        runBtn.getStyleClass().addAll("btn-primary", "btn-full");
        runBtn.setOnAction(e -> { if (onRun != null) onRun.run(); });

        resetBtn = new Button("\uD83D\uDD04  Reset");
        resetBtn.getStyleClass().addAll("btn-secondary", "btn-full");
        resetBtn.setOnAction(e -> { if (onReset != null) onReset.run(); });

        saveBtn = new Button("\uD83D\uDCBE  Save Solution");
        saveBtn.getStyleClass().addAll("btn-secondary", "btn-full");
        saveBtn.setVisible(false); saveBtn.setManaged(false);
        saveBtn.setOnAction(e -> { if (onSave != null) onSave.run(); });

        saveLogBtn = new Button("\uD83D\uDCCB  Save Log");
        saveLogBtn.getStyleClass().addAll("btn-secondary", "btn-full");
        saveLogBtn.setVisible(false); saveLogBtn.setManaged(false);
        saveLogBtn.setOnAction(e -> { if (onSaveLog != null) onSaveLog.run(); });

        box.getChildren().addAll(runBtn, resetBtn, saveBtn, saveLogBtn);
        return box;
    }

    private VBox legend() {
        VBox box = vbox(6);
        box.getStyleClass().add("legend-box");
        box.setPadding(new Insets(10));
        box.getChildren().add(ctrlLabel("Info"));

        for (String[] item : new String[][]{
            {"PLAYER", "Player"},
            {"GOAL",   "Goal"},
            {"ICE",    "Ice Block"},
            {"LAVA",   "Lava"},
            {"🔢", "Checkpoint"}
        }) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            switch (item[0]) {
                case "PLAYER" -> row.getChildren().add(PlayerIcon.make(26));
                case "GOAL"   -> row.getChildren().add(GoalIcon.make(26));
                case "ICE"    -> row.getChildren().add(IcebergIcon.make(22));
                case "LAVA"   -> row.getChildren().add(LavaIcon.make(26));
                default -> row.getChildren().add(new Label(item[0]));
            }
            row.getChildren().add(lbl(item[1], "legend-text"));
            box.getChildren().add(row);
        }
        return box;
    }

    private void pickFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open Board File (.txt)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File f = fc.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (f != null) {
            fileField.setText(f.getAbsolutePath());
            statusLbl.setText("\u2713 " + f.getName());
            statusLbl.getStyleClass().setAll("status-label", "status-ok");
            if (onFileLoaded != null) onFileLoaded.accept(f.getAbsolutePath());
        }
    }

    public void setOnRun(Runnable r)                { onRun     = r; }
    public void setOnReset(Runnable r)              { onReset   = r; }
    public void setOnSave(Runnable r)               { onSave    = r; }
    public void setOnSaveLog(Runnable r)            { onSaveLog = r; }
    public void setOnFileLoaded(Consumer<String> c) { onFileLoaded = c; }

    public void setRunning(boolean running) {
        runBtn.setDisable(running);
        runBtn.setText(running ? "\u23F3  Solving..." : "\uD83D\uDD0D  Run Solver");
    }

    public void setSolvedState(boolean solved) {
        saveBtn   .setVisible(solved); saveBtn   .setManaged(solved);
        saveLogBtn.setVisible(solved); saveLogBtn.setManaged(solved);
    }

    public SolverRequest.Algorithm getAlgorithm() {
        String v = algoCombo.getValue();
        if (v.startsWith("UCS"))  return SolverRequest.Algorithm.UCS;
        if (v.startsWith("GBFS")) return SolverRequest.Algorithm.GBFS;
        if (v.startsWith("BFS"))  return SolverRequest.Algorithm.BFS;
        return SolverRequest.Algorithm.ASTAR;
    }

    public SolverRequest.Heuristic getHeuristic() {
        String v = heurCombo.getValue();
        if (v.startsWith("H2")) return SolverRequest.Heuristic.H2_EUCLIDEAN;
        if (v.startsWith("H3")) return SolverRequest.Heuristic.H3_CHEBYSHEV;
        return SolverRequest.Heuristic.H1_MANHATTAN;
    }

    private static VBox vbox(int s) { return new VBox(s); }

    private static Label sectionTitle(String t) { return lbl(t, "panel-section-title"); }
    private static Label ctrlLabel(String t)    { return lbl(t, "ctrl-label"); }
    private static Label lbl(String t, String cls) {
        Label l = new Label(t); l.getStyleClass().add(cls); return l;
    }
}
