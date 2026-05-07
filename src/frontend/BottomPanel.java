import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;


public class BottomPanel extends VBox {

    private Button tabPlayback, tabLog;

    private Button   firstBtn, prevBtn, playBtn, nextBtn, lastBtn;
    private Slider   stepSlider, speedSlider;
    private TextField jumpField;
    private Label    stepLabel, speedLabel;
    private Pane     playbackPane;

    
    private TableView<LogRow> logTable;
    private ObservableList<LogRow> logData;
    private Pane logPane;

    
    private Runnable      onFirst, onPrev, onPlay, onNext, onLast;
    private IntConsumer   onSeek;
    private Consumer<Double> onSpeedChange;

    public BottomPanel() {
        getStyleClass().add("bottom-panel");
        setPrefHeight(140);
        setMinHeight(120);

        getChildren().addAll(buildTabBar(), buildPlayback(), buildLog());
        showTab(true); 
    }

    

    private HBox buildTabBar() {
        HBox bar = new HBox(0);
        bar.getStyleClass().add("bottom-tab-bar");

        tabPlayback = tabBtn("\u25B6  Playback");
        tabLog      = tabBtn("\uD83D\uDCCB  Search Log");

        tabPlayback.setOnAction(e -> showTab(true));
        tabLog     .setOnAction(e -> showTab(false));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(tabPlayback, tabLog, spacer);
        return bar;
    }

    private Button tabBtn(String t) {
        Button b = new Button(t);
        b.getStyleClass().add("bottom-tab-btn");
        return b;
    }

    private void showTab(boolean isPlayback) {
        tabPlayback.getStyleClass().removeAll("bottom-tab-active");
        tabLog     .getStyleClass().removeAll("bottom-tab-active");
        if (isPlayback) tabPlayback.getStyleClass().add("bottom-tab-active");
        else            tabLog     .getStyleClass().add("bottom-tab-active");

        playbackPane.setVisible(isPlayback);  playbackPane.setManaged(isPlayback);
        logPane     .setVisible(!isPlayback); logPane     .setManaged(!isPlayback);
    }

    

    private Pane buildPlayback() {
        HBox row = new HBox(10);
        row.getStyleClass().add("playback-panel");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 16, 8, 16));
        VBox.setVgrow(row, Priority.ALWAYS);

        firstBtn = pbBtn("\u23EE", () -> { if (onFirst != null) onFirst.run(); });
        prevBtn  = pbBtn("\u25C4", () -> { if (onPrev  != null) onPrev .run(); });
        playBtn  = pbBtn("\u25B6 Play", () -> { if (onPlay != null) onPlay.run(); });
        playBtn.getStyleClass().add("pb-btn-play");
        nextBtn  = pbBtn("\u25BA", () -> { if (onNext  != null) onNext .run(); });
        lastBtn  = pbBtn("\u23ED", () -> { if (onLast  != null) onLast .run(); });

        stepLabel = new Label("Step 0 / 0");
        stepLabel.getStyleClass().add("step-label");

        stepSlider = new Slider(0, 1, 0);
        stepSlider.getStyleClass().add("step-slider");
        HBox.setHgrow(stepSlider, Priority.ALWAYS);
        stepSlider.setMaxWidth(Double.MAX_VALUE);
        stepSlider.valueProperty().addListener((o, ov, nv) -> {
            if (stepSlider.isValueChanging() && onSeek != null)
                onSeek.accept(nv.intValue());
        });

        jumpField = new TextField();
        jumpField.setPromptText("Step #");
        jumpField.setPrefWidth(60);
        jumpField.getStyleClass().add("ctrl-input");
        Button goBtn = pbBtn("Go", () -> {
            try { if (onSeek != null) onSeek.accept(Integer.parseInt(jumpField.getText().trim())); }
            catch (NumberFormatException ignored) {}
        });

        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);

        Label speedLbl = new Label("Speed");
        speedLbl.getStyleClass().add("ctrl-label");
        speedSlider = new Slider(0.25, 3.0, 1.0);
        speedSlider.setPrefWidth(80);
        speedSlider.getStyleClass().add("pb-slider");
        speedLabel = new Label("1.0\u00D7");
        speedLabel.getStyleClass().add("speed-value");
        speedSlider.valueProperty().addListener((o, ov, nv) -> {
            speedLabel.setText(String.format("%.1f\u00D7", nv.doubleValue()));
            if (onSpeedChange != null) onSpeedChange.accept(nv.doubleValue());
        });

        row.getChildren().addAll(
            firstBtn, prevBtn, playBtn, nextBtn, lastBtn,
            stepLabel, stepSlider,
            jumpField, goBtn,
            sep, speedLbl, speedSlider, speedLabel
        );

        playbackPane = row;
        return row;
    }

    

    @SuppressWarnings("unchecked")
    private Pane buildLog() {
        VBox pane = new VBox(0);
        pane.getStyleClass().add("log-pane");
        VBox.setVgrow(pane, Priority.ALWAYS);

        logData  = FXCollections.observableArrayList();
        logTable = new TableView<>(logData);
        logTable.getStyleClass().add("log-table");
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(logTable, Priority.ALWAYS);

        logTable.getColumns().addAll(
            col("#",         "iteration",  50),
            col("Position",  "position",   90),
            col("Move",      "move",       55),
            col("g(n)",      "g",          55),
            col("h(n)",      "h",          60),
            col("f(n)",      "f",          60),
            col("Checkpoint","checkpoint", 90),
            col("Status",    "status",     90)
        );

        
        logTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LogRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-goal","row-skipped","row-visited","row-expanded");
                if (!empty && item != null)
                    getStyleClass().add("row-" + item.getStatus().toLowerCase());
            }
        });

        Label empty = new Label("Run solver to see search log");
        empty.getStyleClass().add("empty-state-text");
        logTable.setPlaceholder(empty);

        pane.getChildren().add(logTable);
        logPane = pane;
        return pane;
    }

    private <T> TableColumn<LogRow, T> col(String title, String prop, int min) {
        TableColumn<LogRow, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMinWidth(min);
        c.getStyleClass().add("log-col");
        return c;
    }

    

    
    public void setTotalSteps(int total) {
        stepSlider.setMax(Math.max(1, total));
        updateStepLabel((int) stepSlider.getValue(), total);
    }
    public void setCurrentStep(int step) {
        stepSlider.setValue(step);
        updateStepLabel(step, (int) stepSlider.getMax());
    }
    public void setPlayingState(boolean p) {
        playBtn.setText(p ? "\u23F8 Pause" : "\u25B6 Play");
    }
    public double getSpeed() { return speedSlider.getValue(); }

    public void setOnFirst(Runnable r)               { onFirst = r; }
    public void setOnPrev(Runnable r)                { onPrev  = r; }
    public void setOnPlay(Runnable r)                { onPlay  = r; }
    public void setOnNext(Runnable r)                { onNext  = r; }
    public void setOnLast(Runnable r)                { onLast  = r; }
    public void setOnSeek(IntConsumer c)             { onSeek  = c; }
    public void setOnSpeedChange(Consumer<Double> c) { onSpeedChange = c; }

    
    public void setLogEntries(List<LogRow> rows) {
        logData.setAll(rows);
    }
    public void clearLog() { logData.clear(); }

    public void highlightLogRow(int idx) {
        if (idx >= 0 && idx < logData.size()) {
            logTable.getSelectionModel().select(idx);
            logTable.scrollTo(idx);
        }
    }

    

    public static class LogRow {
        private final int    iteration;
        private final String position;
        private final String move;
        private final int    g;
        private final double h;
        private final double f;
        private final String checkpoint;
        private final String status;

        public LogRow(int iteration, String position, String move,
                      int g, double h, String checkpoint, String status) {
            this.iteration  = iteration;
            this.position   = position;
            this.move       = move;
            this.g          = g;
            this.h          = h;
            this.f          = g + h;
            this.checkpoint = checkpoint;
            this.status     = status;
        }

        public int    getIteration()  { return iteration; }
        public String getPosition()   { return position; }
        public String getMove()       { return move; }
        public int    getG()          { return g; }
        public double getH()          { return h; }
        public double getF()          { return f; }
        public String getCheckpoint() { return checkpoint; }
        public String getStatus()     { return status; }
    }

    

    private void updateStepLabel(int cur, int total) {
        stepLabel.setText("Step " + cur + " / " + total);
    }

    private Button pbBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("pb-btn");
        b.setOnAction(e -> action.run());
        return b;
    }
}
