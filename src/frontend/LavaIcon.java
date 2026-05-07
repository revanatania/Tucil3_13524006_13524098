import javafx.scene.Node;

public class LavaIcon {
    private static final String[] PATHS = {
        "/frontend/assets/lavafire_icon.png",
        "/assets/lavafire_icon.png",
        "assets/lavafire_icon.png",
    };
    public static Node make(double size) {
        return IconUtil.makeIcon(PATHS, size, "\uD83D\uDD25");
    }
}
