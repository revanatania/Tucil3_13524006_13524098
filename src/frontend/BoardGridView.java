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
    private double     zoomFactor = 1.0;
    private double     viewportWidth = -1;
    private double     viewportHeight = -1;

    private static final double MIN_TILE = 6;
    private static final double MAX_TILE = 96;
    private static final double GAP = 3;
    private static final double FRAME_PADDING = 8;
    private static final double ZOOM_STEP = 1.15;

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

    public void setViewportSize(double width, double height) {
        viewportWidth = width;
        viewportHeight = height;
        recompute();
    }

    public void zoomIn() {
        zoomFactor = Math.min(6.0, zoomFactor * ZOOM_STEP);
        recompute();
        render();
    }

    public void zoomOut() {
        zoomFactor = Math.max(0.2, zoomFactor / ZOOM_STEP);
        recompute();
        render();
    }

    public void resetZoom() {
        zoomFactor = 1.0;
        recompute();
        render();
    }

    public int getZoomPercent() {
        return (int) Math.round(zoomFactor * 100.0);
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

        double w = viewportWidth > 0 ? viewportWidth : (getWidth() > 0 ? getWidth() : getPrefWidth());
        double h = viewportHeight > 0 ? viewportHeight : (getHeight() > 0 ? getHeight() : getPrefHeight());
        double fitByWidth = w > 0 ? (w - FRAME_PADDING - (cols - 1) * GAP) / cols : 56;
        double fitByHeight = h > 0 ? (h - FRAME_PADDING - (rows - 1) * GAP) / rows : 56;
        double fitTile = Math.min(fitByWidth, fitByHeight);
        if (!Double.isFinite(fitTile) || fitTile <= 0) fitTile = 56;

        tileSize = clamp(fitTile * zoomFactor, MIN_TILE, MAX_TILE);
        if (grid != null) applySize();
        setPrefSize(totalWidth(), totalHeight());
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
        grid.setHgap(GAP); grid.setVgap(GAP);
        grid.setAlignment(Pos.CENTER);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid.add(buildTile(r, c), c, r);
        getChildren().setAll(grid);
        setPrefSize(totalWidth(), totalHeight());
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
                double dotSize = clamp(tileSize * 0.16, 4, 14);
                dot.setPrefSize(dotSize, dotSize);
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

    private double totalWidth() {
        return cols * tileSize + (cols - 1) * GAP + FRAME_PADDING;
    }

    private double totalHeight() {
        return rows * tileSize + (rows - 1) * GAP + FRAME_PADDING;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
