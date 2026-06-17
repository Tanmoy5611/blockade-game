package blockaders.view.rules;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RulesView {
    // Rules window width
    private static final int WINDOW_WIDTH = 400;

    // Rules window height
    private static final int WINDOW_HEIGHT = 600;

    // Scene width
    private static final int SCENE_WIDTH = 600;

    // Scene height
    private static final int SCENE_HEIGHT = 800;

    // Logo height in pixels
    private static final int LOGO_HEIGHT = 150;

    // Logo width in pixels
    private static final int LOGO_WIDTH = 250;

    // Text max width in pixels
    private static final int TEXT_MAX_WIDTH = 350;

    // Rules panel width
    private static final int PANEL_WIDTH = 400;

    // Rules panel height
    private static final int PANEL_HEIGHT = 400;

    // Main rules summary text
    private static final String RULES_SUMMARY =
            "Capture opponent's pieces by jumping over them, move horizontally or vertically, and block opponents to win the game.";

    // Step-by-step play rules
    private static final String RULES_PLAY_TEXT = """
            1. On your turn, roll dice matching your colors

            2. Move pieces based on dice rolls; split or combine points freely

            3. Move in any direction; avoid enemy blockades capped with Black pieces

            4. Large pieces cannot move
            """;

    private ImageView logo;
    private Label rules;
    private Label rulesPlay;

    // Creates the rules window
    public RulesView(Stage primaryStage) {
        initializeNodes();
        layoutNodes(primaryStage);
    }

    // Builds the rules logo and text labels
    public void initializeNodes() {
        logo = new ImageView(new Image(getClass().getResource("/Rules.png").toExternalForm()));
        logo.setFitHeight(LOGO_HEIGHT);
        logo.setFitWidth(LOGO_WIDTH);

        rules = new Label(RULES_SUMMARY);
        rules.setWrapText(true);
        rules.setMaxWidth(TEXT_MAX_WIDTH);
        rules.setStyle("-fx-font-color: black ; -fx-font-size: 18; -fx-font-weight: bold;");

        rulesPlay = new Label(RULES_PLAY_TEXT);
        rulesPlay.setMaxWidth(TEXT_MAX_WIDTH);
        rulesPlay.setStyle("-fx-font-color: black ; -fx-font-size: 16;");
    }

    // Places rules content in the given stage
    public void layoutNodes(Stage stage) {

        VBox logoBox = new VBox(10, logo);
        logoBox.setAlignment(Pos.TOP_CENTER);

        VBox vbox = new VBox(20, new Region(), rules, rulesPlay);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle("-fx-padding: 20px;");
        vbox.setPrefWidth(PANEL_WIDTH);
        vbox.setPrefHeight(PANEL_HEIGHT);

        BorderPane root = new BorderPane();
        root.setTop(logoBox);
        root.setCenter(vbox);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setFill(javafx.scene.paint.Color.WHITE);

        stage.setScene(scene);
        stage.setTitle("Game Rules");

        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMaxWidth(WINDOW_WIDTH);
        stage.setMaxHeight(WINDOW_HEIGHT);

        stage.show();
    }

    // Returns the detailed rules label
    public Label getRulesPlay(){
        return rulesPlay;
    }

    // Returns the summary rules label
    public Label getRules() {
        return rules;
    }
}