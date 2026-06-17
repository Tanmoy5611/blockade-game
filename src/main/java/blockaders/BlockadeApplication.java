package blockaders;

import blockaders.model.database.GameDAO;
import blockaders.model.database.MovesDAO;
import blockaders.model.database.PlayerDAO;
import blockaders.presenter.login.LoginPresenter;
import blockaders.view.login.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BlockadeApplication extends Application {
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 800;
    private static final String WINDOW_TITLE = "Blockade - Login";

    @Override
    public void start(Stage primaryStage) {
        LoginView loginView = new LoginView();
        PlayerDAO playerDAO = new PlayerDAO();
        GameDAO gameDAO = new GameDAO();
        MovesDAO movesDAO = new MovesDAO();
        new LoginPresenter(loginView, playerDAO, gameDAO, movesDAO, primaryStage);

        Scene scene = new Scene(loginView, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.show();
    }
}