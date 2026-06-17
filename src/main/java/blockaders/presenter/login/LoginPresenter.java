package blockaders.presenter.login;

import blockaders.model.database.GameDAO;
import blockaders.model.database.MovesDAO;
import blockaders.model.player.HumanPlayer;
import blockaders.model.database.PlayerDAO;
import blockaders.presenter.setup.SetUpPresenter;
import blockaders.view.login.LoginView;
import blockaders.view.setup.SetUpView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginPresenter {
    // Width used for the setup screen
    private static final int WINDOW_WIDTH = 1000;

    // Height used for the setup screen
    private static final int WINDOW_HEIGHT = 800;

    // Message style for successful login or registration
    private static final String SUCCESS_STYLE = "-fx-text-fill: green;";

    // Message style for validation errors
    private static final String ERROR_STYLE = "-fx-text-fill: red;";

    private final LoginView view;
    private final PlayerDAO model;
    private final GameDAO gameDAO;
    private final MovesDAO movesDAO;
    private final Stage stage;

    // Connects the login view to the player database model
    public LoginPresenter(LoginView view, PlayerDAO model, GameDAO gameDAO, MovesDAO movesDAO, Stage stage) {
        this.view = view;
        this.model = model;
        this.gameDAO = gameDAO;
        this.movesDAO = movesDAO;
        this.stage = stage;
        addEventHandlers();
    }

    // Wires login and register buttons
    private void addEventHandlers() {
        view.getLoginButton().setOnAction(e -> login());
        view.getRegisterButton().setOnAction(e -> register());
    }

    // Handles login form submission
    private void login() {
        String username = getUsername();
        String password = getPassword();

        if (hasEmptyInput(username, password)) {
            showError("Empty fields");
            return;
        }

        HumanPlayer player = model.login(username, password);
        if (player == null) {
            showError("Incorrect credentials");
            return;
        }

        showSuccess("Welcome!");
        openSetupScreen(player);
    }

    // Handles registration form submission
    private void register() {
        String username = getUsername();
        String password = getPassword();

        if (hasEmptyInput(username, password)) {
            showError("Empty fields");
            return;
        }

        if (!model.register(username, password)) {
            showError("User already exists");
            return;
        }

        HumanPlayer player = model.login(username, password);
        if (player == null) {
            showError("Error logging in after registration");
            return;
        }

        showSuccess("User registered successfully");
        openSetupScreen(player);
    }

    // Opens the setup screen after authentication
    private void openSetupScreen(HumanPlayer player) {
        SetUpView setUpView = new SetUpView();
        new SetUpPresenter(setUpView, player, gameDAO, movesDAO);
        stage.setScene(new Scene(setUpView, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // Reads the username field
    private String getUsername() {
        return view.getUsernameField().getText().trim();
    }

    // Reads the password field
    private String getPassword() {
        return view.getPasswordField().getText();
    }

    // Checks whether either credential field is empty
    private boolean hasEmptyInput(String username, String password) {
        return username.isBlank() || password.isBlank();
    }

    // Shows a success message in the view
    private void showSuccess(String message) {
        view.getMessageLabel().setStyle(SUCCESS_STYLE);
        view.getMessageLabel().setText(message);
    }

    // Shows an error message in the view
    private void showError(String message) {
        view.getMessageLabel().setStyle(ERROR_STYLE);
        view.getMessageLabel().setText(message);
    }
}