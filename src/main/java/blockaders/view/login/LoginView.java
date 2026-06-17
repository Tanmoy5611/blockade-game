package blockaders.view.login;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {
    // Logo height in pixels
    private static final int LOGO_HEIGHT = 150;

    // Background image percentage size
    private static final int BACKGROUND_SIZE_PERCENT = 50;

    // Main vertical spacing
    private static final int SPACING = 15;

    // Outer padding
    private static final int PADDING = 35;

    // Space between login and register buttons
    private static final int BUTTON_SPACING = 15;

    // Style used for username and password fields
    private static final String INPUT_STYLE = """
            -fx-background-color: #ffffffaa;
            -fx-border-color: #999;
            -fx-background-radius: 8;
            -fx-padding: 4 6 4 6;
            -fx-font-size: 14px;
            """;

    // Style used for login and register buttons
    private static final String BUTTON_STYLE = """
            -fx-font-size: 20px;
            -fx-padding: 10px 35px;
            -fx-background-color: rgba(87,165,197,0.8);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
            """;

    // Style used for validation messages
    private static final String MESSAGE_STYLE = "-fx-font-size: 12px; -fx-font-weight: bold;";

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Button registerButton;
    private final Label messageLabel;

    private ImageView logo;

    // Creates the login view
    public LoginView() {
        this.usernameField = new TextField();
        this.passwordField = new PasswordField();
        this.loginButton = new Button("Login");
        this.registerButton = new Button("Register");
        this.messageLabel = new Label();

        initialiseNodes();
        layoutNodes();
    }

    // Builds form fields, buttons, and logo
    private void initialiseNodes() {
        logo = new ImageView(new Image("logo.png"));
        logo.setPreserveRatio(true);
        logo.setFitHeight(LOGO_HEIGHT);

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");

        usernameField.setStyle(INPUT_STYLE);
        passwordField.setStyle(INPUT_STYLE);
        loginButton.setStyle(BUTTON_STYLE);
        registerButton.setStyle(BUTTON_STYLE);

        messageLabel.setStyle(MESSAGE_STYLE);
    }

    // Places login controls on the screen
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

        setSpacing(SPACING);
        setPadding(new Insets(PADDING));
        setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(BUTTON_SPACING, loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        getChildren().addAll(
                logo,
                usernameField,
                passwordField,
                buttonBox,
                messageLabel
        );
    }

    // Returns the username field
    public TextField getUsernameField() {
        return usernameField;
    }

    // Returns the password field
    public PasswordField getPasswordField() {
        return passwordField;
    }

    // Returns the login button
    public Button getLoginButton() {
        return loginButton;
    }

    // Returns the register button
    public Button getRegisterButton() {
        return registerButton;
    }

    // Returns the message label
    public Label getMessageLabel() {
        return messageLabel;
    }
}