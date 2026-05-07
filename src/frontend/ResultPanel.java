
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
public class ResultPanel extends VBox {
    private Label statusVal, algoVal, heurVal, costVal, timeVal, iterVal, pathVal;
    private Label stepMoveVal, stepCostVal, stepFromVal, stepToVal;
    private VBox  resultContent, emptyState, stepSection;

    public ResultPanel() {
        getStyleClass().add("result-panel");
        setPrefWidth(230);
        setPadding(new Insets(14));
        setSpacing(0);

        emptyState    = buildEmpty();
        resultContent = buildResult();
        resultContent.setVisible(false);
        resultContent.setManaged(false);
        getChildren().addAll(emptyState, resultContent);
    }

    private VBox buildEmpty() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        VBox.setVgrow(box, Priority.ALWAYS);
        Label icon = lbl("\uD83D\uDD0D", "empty-state-icon");
        Label txt  = lbl("Run solver to see results", "empty-state-text");
        txt.setWrapText(true); txt.setMaxWidth(160);
        box.getChildren().addAll(icon, txt);
        return box;
    }

    private VBox buildResult() {
        VBox box = new VBox(0);

        
        VBox summary = section("RESULT");
        statusVal = row(summary, "Status");
        algoVal   = row(summary, "Algorithm");
        heurVal   = row(summary, "Heuristic");
        costVal   = row(summary, "Total Cost");
        timeVal   = row(summary, "Time");
        iterVal   = row(summary, "Iterations");

        VBox pathBox = section("SOLUTION PATH");
        pathVal = new Label();
        pathVal.getStyleClass().add("path-value");
        pathVal.setWrapText(true);
        pathBox.getChildren().add(pathVal);
        stepSection = section("CURRENT STEP");
        stepMoveVal = row(stepSection, "Move");
        stepCostVal = row(stepSection, "Cost so far");
        stepFromVal = row(stepSection, "From");
        stepToVal   = row(stepSection, "To");

        box.getChildren().addAll(summary, pathBox, stepSection);
        return box;
    }


    public void showResult(SolverResultVM vm) {
        toggle(false);
        statusVal.setText(vm.status);
        statusVal.getStyleClass().removeAll("value-ok", "value-error");
        statusVal.getStyleClass().add("Solved".equals(vm.status) ? "value-ok" : "value-error");
        algoVal.setText(vm.algorithmName);
        heurVal.setText(vm.heuristicName);
        costVal.setText(String.valueOf(vm.totalCost));
        timeVal.setText(String.format("%.3f ms", vm.executionTimeMs));
        iterVal.setText(String.valueOf(vm.iterationCount));
        pathVal.setText(vm.solutionPath);
    }

    public void updateStep(BoardStateFrame f) {
        if (f == null) { stepSection.setVisible(false); return; }
        stepSection.setVisible(true);
        stepMoveVal.setText(f.moveDirection);
        stepCostVal.setText(String.valueOf(f.costAtStep));
        String from = (f.slidePath != null && !f.slidePath.isEmpty())
            ? "(" + f.slidePath.get(0)[0] + "," + f.slidePath.get(0)[1] + ")"
            : "—";
        stepFromVal.setText(from);
        stepToVal.setText("(" + f.playerRow + "," + f.playerCol + ")");
    }

    public void reset() { toggle(true); }

    private void toggle(boolean showEmpty) {
        emptyState.setVisible(showEmpty);   emptyState.setManaged(showEmpty);
        resultContent.setVisible(!showEmpty); resultContent.setManaged(!showEmpty);
    }

    private VBox section(String title) {
        VBox box = new VBox(6);
        box.getStyleClass().add("result-section");
        box.setPadding(new Insets(0, 0, 12, 0));
        box.getChildren().add(lbl(title, "panel-section-title"));
        return box;
    }

    private Label row(VBox parent, String key) {
        VBox pair = new VBox(2);
        pair.getChildren().addAll(lbl(key.toUpperCase(), "result-key"), lbl("—", "result-value"));
        parent.getChildren().add(pair);
        return (Label) pair.getChildren().get(1);
    }

    private static Label lbl(String t, String cls) {
        Label l = new Label(t); l.getStyleClass().add(cls); return l;
    }
}
