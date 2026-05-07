import javafx.scene.Node;

public class IcebergIcon {
    private static final String[] PATHS = {
        "/frontend/assets/iceberg.png",
        "/assets/iceberg.png",
        "assets/iceberg.png",
    };
    public static Node make(double size) {
        return IconUtil.makeIcon(PATHS, size, "\uD83E\uDDE7");
    }
}
