package blockaders.presenter.setup;

import blockaders.model.board.Board;
import blockaders.model.database.MovesDAO;
import blockaders.model.game.Game;
import blockaders.model.database.GameDAO;
import blockaders.model.player.HumanPlayer;
import blockaders.model.board.Piece;
import blockaders.model.player.Player;
import blockaders.presenter.board.BoardPresenter;
import blockaders.presenter.board.BoardColdPresenter;
import blockaders.view.board.BoardColdView;
import blockaders.view.board.BoardView;
import blockaders.view.rules.RulesView;
import blockaders.view.setup.ColorsSetUpView;
import blockaders.view.setup.SetUpView;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ColorsSetUpPresenter {
    // Width used for setup and game screens
    private static final int WINDOW_WIDTH = 1000;

    // Height used for setup and game screens
    private static final int WINDOW_HEIGHT = 800;

    // Database value for warm setup
    private static final String WARM = "warm";

    // Database value for cold setup
    private static final String COLD = "cold";

    private final ColorsSetUpView view;
    private final HumanPlayer player;
    private final GameDAO gameDAO;
    private final MovesDAO movesDAO;

    // Connects color setup controls to game creation
    public ColorsSetUpPresenter(ColorsSetUpView view, HumanPlayer player, GameDAO gameDAO, MovesDAO movesDAO) {
        this.view = view;
        this.player = player;
        this.gameDAO = gameDAO;
        this.movesDAO = movesDAO;
        setUpEventHandlers();
    }

    // Wires color setup and navigation buttons
    private void setUpEventHandlers() {
        view.getGameRulesButton().setOnAction(event -> openRules());
        view.getHomeButton().setOnAction(event -> openHome());
        view.getWarmButton().setOnAction(event -> startWarmGame());
        view.getColdButton().setOnAction(event -> startColdGame());
    }

    // Opens the rules window
    private void openRules() {
        new RulesView(new Stage());
    }

    // Returns to the main setup screen
    private void openHome() {
        SetUpView setUpView = new SetUpView();
        new SetUpPresenter(setUpView, player, gameDAO, movesDAO);
        getCurrentStage().setScene(new Scene(setUpView, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // Starts a game where the human controls warm colors
    private void startWarmGame() {
        HumanPlayer warmPlayer = copyPlayerWithColor(Piece.Color.RED);
        int gameId = createGame(WARM);
        if (gameId == GameDAO.NO_GAME_ID) {
            return;
        }

        Board board = new Board();
        BoardView boardView = new BoardView(board);
        Game game = new Game(createPlayerList(warmPlayer));
        new BoardPresenter(board, boardView, warmPlayer, game, gameId, gameDAO, movesDAO);
        getCurrentStage().setScene(new Scene(boardView, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // Starts a game where the human controls cold colors
    private void startColdGame() {
        HumanPlayer coldPlayer = copyPlayerWithColor(Piece.Color.BLUE);
        int gameId = createGame(COLD);
        if (gameId == GameDAO.NO_GAME_ID) {
            return;
        }

        Board board = new Board();
        BoardColdView boardColdView = new BoardColdView(board);
        Game game = new Game(createPlayerList(coldPlayer));
        new BoardColdPresenter(board, boardColdView, coldPlayer, game, gameId, gameDAO, movesDAO);
        getCurrentStage().setScene(new Scene(boardColdView, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // Creates a database game record
    private int createGame(String colorPreference) {
        int gameId = gameDAO.createNewGameInDB(player.getId(), colorPreference);
        if (gameId == GameDAO.NO_GAME_ID) {
            showError("Could not create a new game. Please check the database connection.");
        }
        return gameId;
    }

    // Copies the logged-in player with the selected color
    private HumanPlayer copyPlayerWithColor(Piece.Color color) {
        return new HumanPlayer(player.getId(), player.getName(), player.getScore(), color);
    }

    // Wraps the selected player in a game player list
    private List<Player> createPlayerList(HumanPlayer selectedPlayer) {
        List<Player> players = new ArrayList<>();
        players.add(selectedPlayer);
        return players;
    }

    // Returns the stage currently showing this view
    private Stage getCurrentStage() {
        return (Stage) view.getScene().getWindow();
    }

    // Shows a database error dialog
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}