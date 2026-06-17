package blockaders.presenter.stats;

import blockaders.model.database.DatabaseModel;
import blockaders.model.database.GameDAO;
import blockaders.model.database.MovesDAO;
import blockaders.model.game.Game;
import blockaders.model.game.GameStats;
import blockaders.model.player.HumanPlayer;
import blockaders.model.player.PlayerStats;
import blockaders.presenter.leaderboard.LeaderboardPresenter;
import blockaders.view.leaderboard.LeaderboardView;
import blockaders.presenter.rules.RulesPresenter;
import blockaders.view.rules.RulesView;
import blockaders.presenter.setup.SetUpPresenter;
import blockaders.view.setup.SetUpView;
import blockaders.view.stats.GameStatsView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameStatsPresenter {
    // Width used when returning to setup
    private static final int SETUP_WIDTH = 1000;

    // Height used when returning to setup
    private static final int SETUP_HEIGHT = 800;

    // Format for average move duration
    private static final String AVERAGE_DURATION_FORMAT = "%.2f sec";

    private final GameStatsView view;
    private final LeaderboardView leaderboardView;
    private final LeaderboardPresenter leaderboardPresenter;
    private final Game game;
    private final GameDAO gameDAO;
    private final MovesDAO movesDAO;
    private final HumanPlayer player;

    // Connects the game stats view to game and database data
    public GameStatsPresenter(GameStatsView view, HumanPlayer player, Game game, GameDAO gameDAO, MovesDAO movesDAO) {
        this.view = view;
        this.leaderboardView = new LeaderboardView();
        this.leaderboardPresenter = new LeaderboardPresenter(leaderboardView, player, gameDAO, movesDAO);
        this.game = game;
        this.gameDAO = gameDAO;
        this.movesDAO = movesDAO;
        this.player = player;
        setupListeners();
    }

    // Loads and displays the latest game stats
    public void initialize() {
        GameStats stats = DatabaseModel.fetchGameStats();

        String winnerText = game.getWinnerText();
        view.setWinnerLabel(winnerText);


        view.setPlayTimeLabel("Total Play Time: " + stats.totalPlayTime + " seconds");

        ObservableList<PlayerStats> playerStats = FXCollections.observableArrayList(
                new PlayerStats("Total Moves: " + stats.totalMoves),
                new PlayerStats("Average Duration: " + String.format(AVERAGE_DURATION_FORMAT, stats.avgMoveDuration)),
                new PlayerStats("Score: " + stats.totalScore)
        );
        view.setTableData(playerStats);
    }

    // Wires stats navigation buttons
    private void setupListeners() {
        view.getLeaderboardButton().setOnAction(event -> {
            leaderboardPresenter.initialize();
            leaderboardView.showLeaderboard();
        });

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