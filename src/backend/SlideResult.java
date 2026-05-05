public class SlideResult {
    public static final int OK               = 0;
    public static final int GAME_OVER_BOUNDS = 1;
    public static final int GAME_OVER_LAVA   = 2;
    public static final int GAME_OVER_WRONG_CP = 3;
    public static final int INVALID_MOVE     = 4; 
    
    public int newX;
    public int newY;
    public int moveCost;
    public int tilesTraversed;
    public int status;
    public int newLastCheckpoint;

    public SlideResult(int newX, int newY, int moveCost, int tilesTraversed,
                       int status, int newLastCheckpoint) {
        this.newX = newX;
        this.newY = newY;
        this.moveCost = moveCost;
        this.tilesTraversed = tilesTraversed;
        this.status = status;
        this.newLastCheckpoint = newLastCheckpoint;
    }

    public boolean isValid() {
        return status == OK;
    }
}