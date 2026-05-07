import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class HomeView {

    private final ScrollPane root;
    private final MainApp    app;

    public HomeView(MainApp app) {
        this.app = app;

        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(80, 32, 80, 32));
        content.getStyleClass().add("home-root");
        content.getChildren().addAll(
            buildHeader(),
            buildBoardCard(),
            buildStartButton()
        );

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setFitToHeight(true);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.getStyleClass().add("home-scroll");
    }

    public Pane getRoot() { return (Pane) root.getParent() != null ? (Pane) root.getParent() : wrapInPane(); }
 
    private Pane wrapInPane() {
        StackPane wrap = new StackPane(root);
        wrap.getStyleClass().add("home-root-wrap");
        return wrap;
    }
    
    public ScrollPane getScrollRoot() { return root; }

    private VBox buildHeader() {
        VBox h = new VBox(10);
        h.setAlignment(Pos.CENTER);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER);
        Label ice    = lbl("IcePath", "app-title");
        Label solver = lbl("Solver",  "app-title", "title-accent");
        titleRow.getChildren().addAll(ice, solver);

        Label sub = lbl(
            "UCS  \u00B7  GBFS  \u00B7  A*  \u00B7  BFS  ICE SLIDING PUZZLE VISUALIZER",
            "home-subtitle"
        );

        h.getChildren().addAll(titleRow, sub);
        return h;
    }


    private VBox buildBoardCard() {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("home-board-wrap");
        card.setPadding(new Insets(22, 28, 18, 28));
        card.setMaxWidth(Region.USE_PREF_SIZE);

        card.getChildren().addAll(buildGrid(), buildInfoRow());
        return card;
    }

    private GridPane buildGrid() {
        
        
        
        String[][] cfg = {
            {"empty", "player", "empty"},
            {"empty", "star",   "goal"},
            {"empty", "empty",  "ice"}
        };

        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                grid.add(buildTile(cfg[r][c]), c, r);

        return grid;
    }

    private StackPane buildTile(String type) {
        StackPane tile = new StackPane();
        tile.setPrefSize(76, 76);
        tile.setMinSize(76, 76);
        tile.setMaxSize(76, 76);
        tile.getStyleClass().add("board-tile");

        switch (type) {
            case "player" -> {
                tile.getStyleClass().add("tile-player");
                tile.getChildren().add(PlayerIcon.make(58));
            }
            case "goal" -> {
                tile.getStyleClass().add("tile-goal");
                tile.getChildren().add(GoalIcon.make(54));
            }
            case "ice" -> {
                tile.getStyleClass().add("tile-obstacle");
                tile.getChildren().add(IcebergIcon.make(52));
            }
            case "star" -> {
                tile.getStyleClass().add("tile-empty");
                tile.getChildren().add(lbl("\u2605", "tile-content-icon"));
            }
            default -> tile.getStyleClass().add("tile-empty");
        }
        return tile;
    }

    private HBox buildInfoRow() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
            infoItem("player", "Player"),
            infoItem("goal",   "Goal"),
            infoItem("ice",    "Ice Block")
        );
        return row;
    }

    private HBox infoItem(String type, String label) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        switch (type) {
            case "player" -> row.getChildren().add(PlayerIcon.make(20));
            case "goal"   -> row.getChildren().add(GoalIcon.make(20));
            case "ice"    -> row.getChildren().add(IcebergIcon.make(20));
        }
        row.getChildren().add(lbl(label, "legend-text"));
        return row;
    }

    private Button buildStartButton() {
        Button btn = new Button("\u25BA  Start Solving");
        btn.getStyleClass().addAll("btn-primary", "btn-large");
        btn.setPrefWidth(220);
        btn.setOnAction(e -> app.showSolver());
        return btn;
    }

    private static Label lbl(String t, String... classes) {
        Label l = new Label(t);
        l.getStyleClass().addAll(classes);
        return l;
    }
}
