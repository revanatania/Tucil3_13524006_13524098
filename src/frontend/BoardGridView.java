import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

public class BoardGridView extends StackPane {

    private enum TileType { EMPTY, OBSTACLE, LAVA, GOAL, PLAYER, CHECKPOINT, PATH, VISITED }

    private GridPane   grid;
    private TileType[][] types;
    private int[][]    checkpointNums;
    private int        rows, cols;
    private double     tileSize = 56;

    private static final double MIN_TILE = 24, MAX_TILE = 72;

    public BoardGridView() {
        getStyleClass().add("board-container");
        widthProperty() .addListener((o, ov, nv) -> recompute());
        heightProperty().addListener((o, ov, nv) -> recompute());
    }

    public void loadBoard(char[][] raw) {
        rows = raw.length;
        cols = raw[0].length;
        types          = new TileType[rows][cols];
        checkpointNums = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char ch = raw[r][c];
                types[r][c] = charToType(ch);
                if (ch >= '0' && ch <= '9') checkpointNums[r][c] = ch - '0';
            }
        }
        recompute();
        render();
    }

    public void applyOverlay(int playerRow, int playerCol,
                             List<int[]> solutionPath,
                             List<int[]> visitedCells) {
        if (types == null) return;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (types[r][c] == TileType.PATH || types[r][c] == TileType.VISITED)
                    types[r][c] = TileType.EMPTY;

        if (visitedCells != null)
            for (int[] pos : visitedCells)
                if (inBounds(pos[0], pos[1]) && types[pos[0]][pos[1]] == TileType.EMPTY)
                    types[pos[0]][pos[1]] = TileType.VISITED;

        if (solutionPath != null)
            for (int[] pos : solutionPath)
                if (inBounds(pos[0], pos[1]) && types[pos[0]][pos[1]] == TileType.EMPTY)
                    types[pos[0]][pos[1]] = TileType.PATH;

        if (inBounds(playerRow, playerCol))
            types[playerRow][playerCol] = TileType.PLAYER;

        render();
    }


    private void recompute() {
        if (rows == 0 || cols == 0) return;
        double w = getWidth()  > 0 ? getWidth()  : getPrefWidth();
        double h = getHeight() > 0 ? getHeight() : getPrefHeight();
        if (w <= 0 || h <= 0) return;
        double ts = Math.min((w - 8) / cols, (h - 8) / rows);
        tileSize = Math.max(MIN_TILE, Math.min(ts, MAX_TILE));
        if (grid != null) applySize();
    }

    private void applySize() {
        grid.getChildren().forEach(n -> {
            if (n instanceof StackPane t) {
                t.setPrefSize(tileSize, tileSize);
                t.setMinSize (tileSize, tileSize);
                t.setMaxSize (tileSize, tileSize);
            }
        });
    }

    private void render() {
        grid = new GridPane();
        grid.setHgap(3); grid.setVgap(3);
        grid.setAlignment(Pos.CENTER);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid.add(buildTile(r, c), c, r);
        getChildren().setAll(grid);
        double totalW = cols * (tileSize + 3) + 8;
        double totalH = rows * (tileSize + 3) + 8;
        setPrefSize(totalW, totalH);
    }

    private StackPane buildTile(int r, int c) {
        StackPane tile = new StackPane();
        tile.setPrefSize(tileSize, tileSize);
        tile.setMinSize (tileSize, tileSize);
        tile.setMaxSize (tileSize, tileSize);
        tile.getStyleClass().addAll("board-tile", styleFor(types[r][c]));
        Node content = contentFor(r, c);
        if (content != null) tile.getChildren().add(content);
        return tile;
    }

    private String styleFor(TileType t) {
        return switch (t) {
            case PLAYER     -> "tile-player";
            case OBSTACLE   -> "tile-obstacle";
            case LAVA       -> "tile-lava";
            case GOAL       -> "tile-goal";      
            case CHECKPOINT -> "tile-checkpoint";
            case PATH       -> "tile-path";       
            case VISITED    -> "tile-visited";
            default         -> "tile-empty";
        };
    }

    private Node contentFor(int r, int c) {
        return switch (types[r][c]) {
            case PLAYER     -> PlayerIcon.make(tileSize * 0.9);   
            case GOAL       -> GoalIcon.make(tileSize * 0.9);     
            case OBSTACLE   -> IcebergIcon.make(tileSize * 0.78); 
            case LAVA       -> LavaIcon.make(tileSize * 0.87);    
            case CHECKPOINT -> {
                Label lbl = new Label(String.valueOf(checkpointNums[r][c]));
                lbl.getStyleClass().add("tile-content-text");
                yield lbl;
            }
            case PATH -> {
                StackPane dot = new StackPane();
                dot.setPrefSize(8, 8);
                dot.getStyleClass().add("path-dot");
                yield dot;
            }
            default -> null;
        };
    }

    private Label icon(String emoji) {
        Label lbl = new Label(emoji);
        lbl.getStyleClass().add("tile-content-icon");
        return lbl;
    }

    private TileType charToType(char c) {
        return switch (c) {
            case 'X'       -> TileType.OBSTACLE;
            case 'L'       -> TileType.LAVA;
            case 'S', 'Z'  -> TileType.PLAYER;
            case 'G', 'O'  -> TileType.GOAL;
            default -> (c >= '0' && c <= '9') ? TileType.CHECKPOINT : TileType.EMPTY;
        };
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
}
