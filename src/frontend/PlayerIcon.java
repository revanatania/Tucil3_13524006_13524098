import javafx.scene.Node;
import java.io.InputStream;

public class PlayerIcon {
    private static final String[] PATHS = {
        "/frontend/assets/playerIcon.png",
        "/assets/playerIcon.png",
        "assets/playerIcon.png",
    };
    public static Node make(double size) {
        return IconUtil.makeIcon(PATHS, size, "\u26C4");
    }
}
