package blockaders.presenter.leaderboard;

import blockaders.model.database.GameDAO;
import blockaders.model.database.MovesDAO;
import blockaders.model.player.HumanPlayer;
import blockaders.model.leaderboard.LeaderboardModel;
import blockaders.model.database.DatabaseModel;
import blockaders.presenter.rules.RulesPresenter;
import blockaders.view.rules.RulesView;
import blockaders.presenter.setup.SetUpPresenter;
import blockaders.view.leaderboard.LeaderboardView;
import blockaders.view.setup.SetUpView;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LeaderboardPresenter {
    // Width used when returning to setup screen
    private static final int SETUP_WIDTH = 600;

    // Height used when returning to setup screen
    private static final int SETUP_HEIGHT = 800;

    private final LeaderboardView view;
    private final HumanPlayer player;
    private final GameDAO gameDAO;
    private final MovesDAO movesDAO;

    // Connects the leaderboard view to database-backed model data
    public LeaderboardPresenter(LeaderboardView view, HumanPlayer player, GameDAO gameDAO, MovesDAO movesDAO) {
        this.view = view;
        this.player = player;
        this.gameDAO = gameDAO;
        this.movesDAO = movesDAO;
        setupListeners();
    }

    // Loads leaderboard rows into the view
    public void initialize() {
        ObservableList<LeaderboardModel> leaderboard = DatabaseModel.fetchLeaderboardStats();
        view.updateLeaderboardTable(leaderboard);
    }

    // Wires navigation buttons
    private void setupListeners() {
        view.getGameRulesButton().setOnAction(event -> {
            RulesView rulesView = new RulesView(new Stage());
            new RulesPresenter(rulesView);
            rulesView.layoutNodes(new Stage());
        });

        view.getHomeButton().setOnAction(event -> {
            Stage currentStage = (Stage) view.getHomeButton().getScene().getWindow();
            SetUpView setUpView = new SetUpView();
            new SetUpPresenter(setUpView, player, gameDAO, movesDAO);
            Scene startScene = new Scene(setUpView, SETUP_WIDTH, SETUP_HEIGHT);
            currentStage.setScene(startScene);
        });
    }
}