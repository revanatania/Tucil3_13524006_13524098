import javafx.scene.Node;

public class GoalIcon {
    private static final String[] PATHS = {
        "/frontend/assets/goal_icon.png",
        "/assets/goal_icon.png",
        "assets/goal_icon.png",
    };
    public static Node make(double size) {
        return IconUtil.makeIcon(PATHS, size, "\uD83C\uDFAF", false);
    }
}
