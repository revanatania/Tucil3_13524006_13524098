import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    private Stage      primaryStage;
    private BorderPane shell;
    private Button     navHome, navSolver;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("IcePath Solver");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        shell = new BorderPane();
        shell.setTop(buildNavBar());

        Scene scene = new Scene(shell, 1440, 900);
        applyCSS(scene);

        stage.setScene(scene);
        showHome();
        stage.show();
    }

    private void applyCSS(Scene scene) {
        
        String[] cssPaths = {
            "/frontend/app.css",
            "/app.css",
            "frontend/app.css",
        };
        for (String path : cssPaths) {
            URL url = getClass().getResource(path);
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
                System.out.println("[MainApp] CSS loaded from: " + path);
                return;
            }
        }
        System.err.println("[MainApp] WARNING: app.css not found in resources!");
    }

    private HBox buildNavBar() {
        HBox bar = new HBox(0);
        bar.getStyleClass().add("nav-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        
        HBox brand = new HBox(8);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 24, 0, 20));
        Label crystal = new Label("\u2744");
        crystal.getStyleClass().add("nav-brand-icon");
        Label brandName = new Label("IcePath Solver");
        brandName.getStyleClass().add("nav-brand-name");
        brand.getChildren().addAll(crystal, brandName);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        navHome   = navBtn("Home");
        navSolver = navBtn("Solver");
        navHome  .setOnAction(e -> showHome());
        navSolver.setOnAction(e -> showSolver());

        HBox navBtns = new HBox(4);
        navBtns.setAlignment(Pos.CENTER);
        navBtns.setPadding(new Insets(0, 16, 0, 0));
        navBtns.getChildren().addAll(navHome, navSolver);

        bar.getChildren().addAll(brand, spacer, navBtns);
        return bar;
    }

    private Button navBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-btn");
        return b;
    }

    private void setActiveNav(Button active) {
        navHome  .getStyleClass().removeAll("nav-btn-active");
        navSolver.getStyleClass().removeAll("nav-btn-active");
        active.getStyleClass().add("nav-btn-active");
    }

    public void showHome() {
        setActiveNav(navHome);
        try {
            shell.setCenter(new HomeView(this).getScrollRoot());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Home view: " + e.getMessage());
        }
    }

    public void showSolver() {
        setActiveNav(navSolver);
        try {
            shell.setCenter(new SolverView(this).getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Solver view: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Label err = new Label("Error: " + msg);
        err.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 20;");
        shell.setCenter(err);
    }

    public static void main(String[] args) { launch(args); }
}
