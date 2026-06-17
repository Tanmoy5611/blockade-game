package blockaders.view.setup;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class SetUpView extends BorderPane {
    // Setup screen width
    private static final int WINDOW_WIDTH = 1000;

    // Setup screen height
    private static final int WINDOW_HEIGHT = 800;

    // Logo height in pixels
    private static final int LOGO_HEIGHT = 150;

    // Main action button width
    private static final int ACTION_BUTTON_WIDTH = 250;

    // Navigation button width
    private static final int NAV_BUTTON_WIDTH = 200;

    // Background image percentage size
    private static final int BACKGROUND_SIZE_PERCENT = 100;

    // Style used for setup buttons
    private static final String BUTTON_STYLE = """
            -fx-font-size: 20px;
            -fx-padding: 12px 20px;
            -fx-background-color: rgba(87,165,197,0.8);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
            """;

    private Button startButton;
    private Button gameRulesButton;
    private Button leaderboardButton;
    private ImageView logo;


    // Creates the setup view
    public SetUpView() {
        initialiseNodes();
        layoutNodes();
        setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    // Builds setup buttons and logo
    private void initialiseNodes() {
        logo = new ImageView(new Image("logo.png"));
        logo.setPreserveRatio(true);
        logo.setFitHeight(LOGO_HEIGHT);

        startButton = new Button("Start");
        startButton.setPrefWidth(ACTION_BUTTON_WIDTH);

        leaderboardButton = new Button("Leaderboard");
        leaderboardButton.setPrefWidth(ACTION_BUTTON_WIDTH);

        gameRulesButton = new Button("Game Rules");
        gameRulesButton.setPrefWidth(NAV_BUTTON_WIDTH);

        startButton.setStyle(BUTTON_STYLE);
        leaderboardButton.setStyle(BUTTON_STYLE);
        gameRulesButton.setStyle(BUTTON_STYLE);
    }

    // Places setup controls on the screen
    private void layoutNodes() {
        Image backgroundImage = new Image(getClass().getResource("/background.png").toExternalForm());

        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BACKGROUND_SIZE_PERCENT, BACKGROUND_SIZE_PERCENT, true, true, true, false)
        );

        setBackground(new Background(background));

        VBox logoBox = new VBox(logo);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 10, 0));

        VBox vbox = new VBox(15, startButton, leaderboardButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10, 40, 10, 40));
        setCenter(vbox);

        HBox navBar = new HBox(20, gameRulesButton);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(15, 0, 15, 0));
        setBottom(navBar);

        setTop(logoBox);
    }

    // Returns the start button
    public Button getStartButton() {
        return startButton;
    }

    // Returns the rules button
    public Button getGameRulesButton() {
        return gameRulesButton;
    }

    // Returns the leaderboard button
    public Button getLeaderboardButton() {
        return leaderboardButton;
    }
}