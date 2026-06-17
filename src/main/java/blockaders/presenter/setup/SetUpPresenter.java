package blockaders.presenter.setup;

import blockaders.model.database.GameDAO;
import blockaders.model.database.MovesDAO;
import blockaders.model.player.HumanPlayer;
import blockaders.view.setup.ColorsSetUpView;
import blockaders.presenter.leaderboard.LeaderboardPresenter;
import blockaders.view.leaderboard.LeaderboardView;
import blockaders.view.rules.RulesView;
import blockaders.view.setup.SetUpView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SetUpPresenter {
    // Width used for setup navigation screens
    private static final int WINDOW_WIDTH = 1000;

    // Height used for setup navigation screens
    private static final int WINDOW_HEIGHT = 800;

    private final SetUpView view;
    private final HumanPlayer player;
    private final GameDAO gameDAO;
    private final MovesDAO movesDAO;

    // Connects the setup view to navigation actions
    public SetUpPresenter(SetUpView view, HumanPlayer player, GameDAO gameDAO, MovesDAO movesDAO) {
        this.view = view;
        this.player = player;
        this.gameDAO = gameDAO;
        this.movesDAO = movesDAO;
        setupListeners();
    }

    // Wires setup navigation buttons
    private void setupListeners() {
        view.getStartButton().setOnAction(event -> openColorSetup());
        view.getLeaderboardButton().setOnAction(event -> openLeaderboard());
        view.getGameRulesButton().setOnAction(event -> openRules());
    }

    // Opens the color selection screen
    private void openColorSetup() {
        closeUnfinishedGame();

        Stage stage = getCurrentStage();
        ColorsSetUpView colorsSetUpView = new ColorsSetUpView();
        new ColorsSetUpPresenter(colorsSetUpView, player, gameDAO, movesDAO);
        stage.setScene(new Scene(colorsSetUpView, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // Closes any previous unfinished game for this player
    private void closeUnfinishedGame() {
        int oldGameId = GameDAO.getLatestUnfinishedGameId(player.getId());
        if (oldGameId != GameDAO.NO_GAME_ID) {
            gameDAO.endGame(oldGameId);
        }
    }

    // Opens the leaderboard window
    private void openLeaderboard() {
        LeaderboardView leaderboardView = new LeaderboardView();
        LeaderboardPresenter leaderboardPresenter = new LeaderboardPresenter(leaderboardView, player, gameDAO, movesDAO);
        leaderboardPresenter.initialize();
        leaderboardView.showLeaderboard();
    }

    // Opens the rules window
    private void openRules() {
        new RulesView(new Stage());
    }

    // Returns the stage currently showing this view
    private Stage getCurrentStage() {
        return (Stage) view.getScene().getWindow();
    }
}