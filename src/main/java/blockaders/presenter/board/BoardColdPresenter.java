package blockaders.presenter.board;

import blockaders.model.board.Board;
import blockaders.model.board.Piece;
import blockaders.model.board.Position;
import blockaders.model.database.GameDAO;
import blockaders.model.database.MovesDAO;
import blockaders.model.game.Dice;
import blockaders.model.game.Game;
import blockaders.model.player.Bot;
import blockaders.model.player.HumanPlayer;
import blockaders.presenter.stats.GameStatsPresenter;
import blockaders.view.board.BoardColdView;
import blockaders.view.stats.GameStatsView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.Optional;

public class BoardColdPresenter {
    // Seconds added to the turn timer at each tick
    private static final int TIMER_STEP_SECONDS = 1;

    // Delay before the bot starts its turn
    private static final int BOT_TURN_DELAY_MILLIS = 1000;

    // Default timer and score value
    private static final int ZERO_VALUE = 0;

    // Small triangle size in pixels
    private static final double SMALL_TRIANGLE_SIZE = 20;

    // Medium triangle size in pixels
    private static final double MEDIUM_TRIANGLE_SIZE = 30;

    // Large triangle size in pixels
    private static final double LARGE_TRIANGLE_SIZE = 40;

    // Half divisor used when drawing triangles
    private static final double HALF_DIVISOR = 2;

    // Default cell style after highlights are cleared
    private static final String DEFAULT_CELL_STYLE =
            "-fx-border-color: #333; -fx-background-color: white; -fx-border-width: 3.5px; -fx-border-radius: 5px; -fx-background-radius: 5px;";

    // Highlight style for valid destinations
    private static final String VALID_DESTINATION_STYLE = "-fx-background-color: green; -fx-border-width: 2;";

    private final Board model;
    private final BoardColdView view;
    private final Bot bot;
    private final HumanPlayer currentPlayer;
    private final Dice blueDice = new Dice("BLUE"), greenDice = new Dice("GREEN");

    private Position selected = null;
    private Timeline turnTimer;
    private int timeElapsed, blueMovementPoints = 0, greenMovementPoints = 0;
    private boolean diceRolled = false, isPlayerTurn = true, gameEnded = false;
    private int warmColorScore = 0, coldColorScore = 0;
    private final int currentGameId;

    private final GameDAO gameDAO;
    private final MovesDAO movesDAO;
    private final Game game;

    // Connects the cold board view to the game model
    public BoardColdPresenter(
            Board model,
            BoardColdView view,
            HumanPlayer currentPlayer,
            Game game,
            int currentGameId,
            GameDAO gameDAO,
            MovesDAO movesDAO
    ) {
        this.model = model;
        this.view = view;
        this.currentPlayer = currentPlayer;
        this.game = game;
        this.currentGameId = currentGameId;
        this.gameDAO = gameDAO;
        this.movesDAO = movesDAO;
        this.bot = new Bot(model, Piece.Color.BLUE, Piece.Color.GREEN);

        addEventHandlers();
        updateView();
    }

    // Wires all board buttons and cells to presenter actions
    private void addEventHandlers() {
        initializeCellListeners();

        view.getRollButton().setOnAction(evt -> {
            if (gameEnded || !isPlayerTurn || diceRolled) {
                showAlert("You must roll the dice only once per turn.");
                return;
            }
            rollDice();
            view.setDiceValues(blueMovementPoints, greenMovementPoints);
            updateView();
            startTurnTimer();
        });

        view.getEndTurnButton().setOnAction(evt -> {
            if (gameEnded || !isPlayerTurn || !diceRolled) {
                showAlert("You must roll the dice before ending your turn.");
                return;
            }
            endPlayerTurn();
        });

        view.getEndGameButton().setOnAction(evt -> {
            if (!gameEnded && confirmEndGame()) endGameManually();
        });
    }

    // Adds click handlers to every board cell
    private void initializeCellListeners() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                final int r = row, c = col;
                view.getCellNode(r, c).setOnMouseClicked(e -> {
                    if (!gameEnded && isPlayerTurn) handleCellClick(r, c);
                });
            }
        }
    }

    // Handles selecting a piece or moving the selected piece
    private void handleCellClick(int row, int col) {
        if (!diceRolled) {
            showAlert("Please roll the dice before moving.");
            return;
        }

        Position clicked = model.getCell(row, col);

        if (selected == null) {
            if (!clicked.getStack().isEmpty()) {
                selected = clicked;
                clearHighlights();
                highlightValidDestinations(clicked);
            }
        } else {
            Piece piece = selected.getStack().peek();

            if (piece.getSize() == Piece.Size.LARGE) {
                showAlert("Large pieces cannot be moved.");
                selected = null;
                clearHighlights();
                return;
            }

            attemptMove(row, col);

            selected = null;
            clearHighlights();
        }

        view.setDiceValues(blueMovementPoints, greenMovementPoints);
        updateView();

        if (blueMovementPoints == 0 && greenMovementPoints == 0) {
            showAlert("No moves left, please end your turn.");
        }
    }


    // Attempts to move the selected piece to the clicked cell
    private void attemptMove(int row, int col) {
        Position to = model.getCell(row, col);
        Piece piece = selected.getStack().peek();

        if (!model.isValidMove(selected, to, piece)) {
            showAlert("Invalid move: movement can be in any direction, but cannot jump over invalid cells.");
            return;
        }

        int cost = model.calculateMoveCost(selected, to, piece);
        if (cost < 0) {
            showAlert("Invalid move: cannot land or jump over empty cells.");
            return;
        }

        String color = piece.getColor().toString();
        if (color.equalsIgnoreCase(blueDice.getColor())) {
            if (blueMovementPoints >= cost) {
                movePiece(selected.getRow(), selected.getCol(), row, col, piece);
                blueMovementPoints -= cost;
            } else {
                showAlert("You don't have enough movement points for that move.");
                return;
            }
        } else if (color.equalsIgnoreCase(greenDice.getColor())) {
            if (greenMovementPoints >= cost) {
                movePiece(selected.getRow(), selected.getCol(), row, col, piece);
                greenMovementPoints -= cost;
            } else {
                showAlert("You don't have enough movement points for that move.");
                return;
            }
        } else {
            showAlert("Invalid piece color for movement.");
            return;
        }

        updateView();
    }

    // Moves a piece, saves it, and checks winner state
    private void movePiece(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        model.getCell(fromRow, fromCol).removeTopPiece();
        model.getCell(toRow, toCol).placePiece(piece);

        saveMove(fromRow, fromCol, toRow, toCol, piece);

        Piece.Color treeColor = model.checkAndBlockTree(toRow, toCol);
        if (treeColor != null) {
            if (treeColor == Piece.Color.RED || treeColor == Piece.Color.YELLOW) warmColorScore++;
            else if (treeColor == Piece.Color.BLUE || treeColor == Piece.Color.GREEN) coldColorScore++;
            showAlert("Tree formed by " + treeColor + "! +1 point");
        }

        updateScores();
        view.updateScore(warmColorScore, coldColorScore);
        view.refresh();

        if (model.hasColorCompletedAllTrees(Piece.Color.BLUE) || model.hasColorCompletedAllTrees(Piece.Color.GREEN)) {
            showAlert("Cold player has won!");
            endGame();
        } else if (model.hasColorCompletedAllTrees(Piece.Color.RED) || model.hasColorCompletedAllTrees(Piece.Color.YELLOW)) {
            showAlert("Warm player has won!");
            endGame();
        }
    }

    // Saves one move and the current score
    private void saveMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        try {
            movesDAO.saveMove(currentGameId, currentPlayer.getId(), timeElapsed, fromRow, fromCol, toRow, toCol, piece.getPieceId());
            gameDAO.saveScore(currentGameId, currentPlayer.getId(), warmColorScore + coldColorScore);
        } catch (Exception e) {
            showAlert("The move could not be saved.");
        }
    }

    // Recalculates warm and cold scores from the board
    private void updateScores() {
        coldColorScore = model.getColdScore();
        warmColorScore = model.getWarmScore();
    }

    // Rolls the cold dice for the current turn
    private void rollDice() {
        blueMovementPoints = blueDice.roll();
        greenMovementPoints = greenDice.roll();
        diceRolled = true;
    }

    // Ends the human turn and lets the bot play
    private void endPlayerTurn() {
        diceRolled = false;
        selected = null;
        isPlayerTurn = false;
        if (turnTimer != null) turnTimer.stop();
        view.updateTimer(ZERO_VALUE);
        updateView();

        new Thread(() -> {
            try { Thread.sleep(BOT_TURN_DELAY_MILLIS); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                if (!gameEnded) {
                    bot.playFullTurn();
                    updateView();
                    checkBotVictory();
                    if (!gameEnded) {
                        isPlayerTurn = true;
                        diceRolled = false;
                        view.updateTimer(ZERO_VALUE);
                    }
                }
            });
        }).start();
    }

    // Checks whether the bot won after its turn
    private void checkBotVictory() {
        if (model.hasColorCompletedAllTrees(Piece.Color.BLUE) || model.hasColorCompletedAllTrees(Piece.Color.GREEN)) {
            showAlert("Cold player (Bot) has won!");
            endGame();
        } else if (model.hasColorCompletedAllTrees(Piece.Color.RED) || model.hasColorCompletedAllTrees(Piece.Color.YELLOW)) {
            showAlert("Warm player has won!");
            endGame();
        }
    }

    // Redraws all board pieces and score values
    private void updateView() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                StackPane cell = view.getCellNode(row, col);
                cell.getChildren().clear();
                for (Piece piece : model.getCell(row, col).getStack()) {
                    Polygon triangle = createTriangle(piece.getSize());
                    triangle.setFill(mapColor(piece.getColor()));
                    triangle.setStroke(Color.BLACK);
                    cell.getChildren().add(triangle);
                }
            }
        }
        updateScores();
        view.updateScore(warmColorScore, coldColorScore);
        view.refresh();
    }

    // Creates a triangle shape for one piece size
    private Polygon createTriangle(Piece.Size size) {
        double base = switch (size) {
            case SMALL -> SMALL_TRIANGLE_SIZE;
            case MEDIUM -> MEDIUM_TRIANGLE_SIZE;
            case LARGE -> LARGE_TRIANGLE_SIZE;
        };
        double half = base / HALF_DIVISOR;
        return new Polygon(0.0, -half, -half, half, half, half);
    }

    // Maps game colors to JavaFX colors
    private Color mapColor(Piece.Color color) {
        return switch (color) {
            case RED -> Color.RED;
            case GREEN -> Color.GREEN;
            case BLUE -> Color.BLUE;
            case YELLOW -> Color.GOLD;
            case CLEAR -> Color.LIGHTGRAY;
            case BLACK -> Color.BLACK;
        };
    }

    // Shows an information dialog
    private void showAlert(String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Starts the timer for the current player turn
    private void startTurnTimer() {
        if (turnTimer != null) turnTimer.stop();
        timeElapsed = ZERO_VALUE;
        view.updateTimer(timeElapsed);
        turnTimer = new Timeline(new KeyFrame(Duration.seconds(TIMER_STEP_SECONDS), e -> {
            timeElapsed++;
            view.updateTimer(timeElapsed);
        }));
        turnTimer.setCycleCount(Timeline.INDEFINITE);
        turnTimer.play();
    }

    // Confirms whether the player wants to end the game
    private boolean confirmEndGame() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm End Game");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to end the game manually?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Ends the game after manual confirmation
    private void endGameManually() {
        showAlert("The game has been ended manually.");
        endGame();
    }

    // Ends the game, saves the result, and opens stats
    private void endGame() {
        gameEnded = true;
        isPlayerTurn = false;
        if (turnTimer != null) turnTimer.stop();

        gameDAO.endGame(currentGameId);
        gameDAO.saveScore(currentGameId, currentPlayer.getId(), warmColorScore + coldColorScore);

        view.getRollButton().setDisable(true);
        view.getEndTurnButton().setDisable(true);
        view.updateTimer(0);

        game.setColdScore(coldColorScore);
        game.setWarmScore(warmColorScore);

        Platform.runLater(() -> {
            GameStatsView statsView = new GameStatsView();
            GameStatsPresenter statsPresenter = new GameStatsPresenter(statsView, currentPlayer, game, gameDAO, movesDAO);
            statsPresenter.initialize();
            statsView.show();
        });
    }

    // Removes all valid-move highlights from the board
    private void clearHighlights() {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                view.getCellNode(r, c).setStyle("-fx-border-color: #333; -fx-background-color: white; -fx-border-width: 3.5px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    }

    // Highlights destinations that the selected piece can afford
    private void highlightValidDestinations(Position from) {
        Piece movingPiece = from.getStack().peek();
        Piece.Color pieceColor = movingPiece.getColor();
        int availablePoints = (pieceColor == Piece.Color.BLUE) ? blueMovementPoints : greenMovementPoints;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Position to = model.getCell(r, c);
                if (model.isValidMove(from, to, movingPiece) && model.canLandOn(to, movingPiece)) {
                    int cost = model.calculateMoveCost(from, to, movingPiece);
                    if (cost >= 0 && cost <= availablePoints) {
                        view.getCellNode(r, c).setStyle("-fx-background-color: green; -fx-border-width: 2;");
                    }
                }
            }
        }
    }

}