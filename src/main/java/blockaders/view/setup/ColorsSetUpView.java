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

public class ColorsSetUpView extends BorderPane {
    // Color setup screen width
    private static final int WINDOW_WIDTH = 1000;

    // Color setup screen height
    private static final int WINDOW_HEIGHT = 800;

    // Logo height in pixels
    private static final int LOGO_HEIGHT = 150;

    // Main color button width
    private static final int COLOR_BUTTON_WIDTH = 250;

    // Navigation button width
    private static final int NAV_BUTTON_WIDTH = 200;

    // Background image percentage size
    private static final int BACKGROUND_SIZE_PERCENT = 100;

    // Style used for default setup buttons
    private static final String DEFAULT_BUTTON_STYLE = """
            -fx-font-size: 20px;
            -fx-padding: 12px 20px;
            -fx-background-color: rgba(87,165,197,0.8);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
            """;

    // Style used for the warm color button
    private static final String WARM_BUTTON_STYLE = """
            -fx-font-size: 20px;
            -fx-padding: 12px 20px;
            -fx-background-color: rgb(248,115,45);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
            """;

    private Button coldButton;
    private Button warmButton;
    private Button gameRulesButton;
    private Button homeButton;
    private ImageView logo;


    // Creates the color setup view
    public ColorsSetUpView() {
        initialiseNodes();
        layoutNodes();
        setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    // Builds color buttons, navigation buttons, and logo
    private void initialiseNodes() {
        logo = new ImageView(new Image("logo.png"));
        logo.setPreserveRatio(true);
        logo.setFitHeight(LOGO_HEIGHT);

        coldButton = new Button("Cold Colours");
        coldButton.setPrefWidth(COLOR_BUTTON_WIDTH);

        warmButton = new Button("Warm Colours");
        warmButton.setPrefWidth(COLOR_BUTTON_WIDTH);

        gameRulesButton = new Button("Game Rules");
        homeButton = new Button("Home");
        homeButton.setPrefWidth(NAV_BUTTON_WIDTH);
        gameRulesButton.setPrefWidth(NAV_BUTTON_WIDTH);

        warmButton.setStyle(WARM_BUTTON_STYLE);
        coldButton.setStyle(DEFAULT_BUTTON_STYLE);
        gameRulesButton.setStyle(DEFAULT_BUTTON_STYLE);
        homeButton.setStyle(DEFAULT_BUTTON_STYLE);
    }

    // Places color setup controls on the screen
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

        VBox vbox = new VBox(15, coldButton, warmButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10, 40, 10, 40));
        setCenter(vbox);

        HBox navBar = new HBox(20, gameRulesButton, homeButton);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(15, 0, 15, 0));
        setBottom(navBar);

        setTop(logoBox);
    }

    // Returns the cold color button
    public Button getColdButton() {
        return coldButton;
    }

    // Returns the warm color button
    public Button getWarmButton() {
        return warmButton;
    }

    // Returns the rules button
    public Button getGameRulesButton() {
        return gameRulesButton;
    }

    // Returns the home button
    public Button getHomeButton() {
        return homeButton;
    }

}