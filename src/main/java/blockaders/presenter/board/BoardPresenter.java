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
import blockaders.view.board.BoardView;
import blockaders.view.stats.GameStatsView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.Optional;
import java.util.Stack;

public class BoardPresenter {
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
            "-fx-border-color: #333; -fx-border-width: 3.5px; -fx-background-color: white; -fx-border-radius: 5px; -fx-background-radius: 5px;";

    // Highlight style for valid destinations
    private static final String VALID_DESTINATION_STYLE = "-fx-background-color: green; -fx-border-width: 2;";

    private final Board model;
    private final BoardView view;
    private final Bot bot;
    private final HumanPlayer currentPlayer;
    private Timeline turnTimer;
    private int timeElapsed;

    private Position selected = null;
    private final Dice redDice = new Dice("RED");
    private final Dice yellowDice = new Dice("YELLOW");

    private int redMovementPoints = 0;
    private int yellowMovementPoints = 0;
    private boolean diceRolled = false;
    private boolean isPlayerTurn = true;
    private boolean gameEnded = false;

    private int warmColorScore = 0;
    private int coldColorScore = 0;

    private final int currentGameId;
    private final GameDAO gameDAO;
    private final MovesDAO movesDAO;
    private final Game game;

    // Connects the warm board view to the game model
    public BoardPresenter(
            Board model,
            BoardView view,
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

        this.bot = new Bot(model, Piece.Color.RED, Piece.Color.YELLOW);
        this.gameDAO = gameDAO;
        this.movesDAO = movesDAO;

        addEventHandlers();
        updateView();
    }

    // Wires all board buttons and cells to presenter actions
    private void addEventHandlers() {
        initializeCellListeners();

        view.getRollButton().setOnAction(evt -> {
            if (gameEnded || !isPlayerTurn) return;
            if (diceRolled) {
                showAlert("You have already rolled the dice this turn.");
            } else {
                rollDice();
                view.setDiceValues(redMovementPoints, yellowMovementPoints);
                updateView();
                startTurnTimer();
            }
        });

        view.getEndTurnButton().setOnAction(evt -> {
            if (gameEnded || !isPlayerTurn) return;
            if (!diceRolled) {
                showAlert("You must roll the dice first.");
                return;
            }
            if (redMovementPoints > 0 || yellowMovementPoints > 0) {
                showAlert("You still have available moves.");
                return;
            }
            endPlayerTurn();
        });
        view.getEndGameButton().setOnAction(evt -> {
            if (gameEnded) return;
            if (confirmEndGame()) {
                endGameManually();
            }
        });
    }

    // Adds click handlers to every board cell
    private void initializeCellListeners() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                final int r = row, c = col;
                view.getCellNode(row, col).setOnMouseClicked(event -> {
                    if (!gameEnded && isPlayerTurn) {
                        handleCellClick(r, c);
                    }
                });
            }
        }
    }

    // Starts the timer for the current player turn
    private void startTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
        }

        timeElapsed = ZERO_VALUE;
        view.updateTimer(timeElapsed);

        turnTimer = new Timeline(new KeyFrame(Duration.seconds(TIMER_STEP_SECONDS), event -> {
            timeElapsed++;
            view.updateTimer(timeElapsed);
        }));
        turnTimer.setCycleCount(Timeline.INDEFINITE);
        turnTimer.play();
    }

    // Ends the human turn and lets the bot play
    private void endPlayerTurn() {
        diceRolled = false;
        selected = null;
        redMovementPoints = ZERO_VALUE;
        yellowMovementPoints = ZERO_VALUE;
        isPlayerTurn = false;

        if (turnTimer != null) {
            turnTimer.stop();
        }

        view.updateTimer(ZERO_VALUE);
        updateView();

        new Thread(() -> {
            try {
                Thread.sleep(BOT_TURN_DELAY_MILLIS);
            } catch (InterruptedException ignored) {}

            Platform.runLater(() -> {
                if (!gameEnded) {
                    bot.playFullTurn();
                    updateView();
                    isPlayerTurn = true;
                }
            });
        }).start();
    }

    // Rolls the warm dice for the current turn
    private void rollDice() {
        redMovementPoints = redDice.roll();
        yellowMovementPoints = yellowDice.roll();
        diceRolled = true;
    }

    // Handles selecting a piece or moving the selected piece
    private void handleCellClick(int row, int col) {
        if (!diceRolled) {
            showAlert("You must roll the dice before moving.");
            return;
        }

        Position clicked = model.getCell(row, col);

        if (selected == null) {
            if (!clicked.getStack().isEmpty()) {
                selected = clicked;
                highlightValidDestinations(selected);
            }
        } else {
            Piece piece = selected.getStack().peek();
            int moveCost = model.calculateMoveCost(selected, clicked, piece, true);
            if (moveCost < 0) {
                showAlert("Invalid move.");
            } else {
                String color = piece.getColor().toString();
                boolean canMove = false;

                if (color.equalsIgnoreCase(redDice.getColor()) && redMovementPoints >= moveCost) {
                    redMovementPoints -= moveCost;
                    canMove = true;
                } else if (color.equalsIgnoreCase(yellowDice.getColor()) && yellowMovementPoints >= moveCost) {
                    yellowMovementPoints -= moveCost;
                    canMove = true;
                }

                if (canMove) {
                    movePiece(selected.getRow(), selected.getCol(), row, col, piece);
                } else {
                    showAlert("You don't have enough movement points for that piece.");
                }
            }
            selected = null;
            clearHighlights();
            view.setDiceValues(redMovementPoints, yellowMovementPoints);
            updateView();

            if (redMovementPoints == 0 && yellowMovementPoints == 0) {
                showAlert("You have no available moves. End your turn.");
            }
        }
    }

    // Removes all valid-move highlights from the board
    private void clearHighlights() {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                view.getCellNode(r, c).setStyle(DEFAULT_CELL_STYLE);
            }
        }
    }

    // Highlights all legal destinations for the selected piece
    private void highlightValidDestinations(Position from) {
        Piece movingPiece = from.getStack().peek();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Position to = model.getCell(r, c);
                if (model.isValidMove(from, to, movingPiece) && model.canLandOn(to, movingPiece)) {
                    view.getCellNode(r, c).setStyle(VALID_DESTINATION_STYLE);
                }
            }
        }
    }

    // Moves a piece and checks whether the game has been won
    private void movePiece(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        Position from = model.getCell(fromRow, fromCol);
        from.removeTopPiece();
        model.getCell(toRow, toCol).placePiece(piece);

        int pieceId = piece.getPieceId();
        movesDAO.saveMove(currentGameId, currentPlayer.getId(), timeElapsed, fromRow, fromCol, toRow, toCol, pieceId);

        Piece.Color formedTreeColor = model.checkAndBlockTree(toRow, toCol);
        if (formedTreeColor != null) {
            showAlert("Tree formed! +1 point");
        }

        updateScores();
        view.updateScore(warmColorScore, coldColorScore);

        if (model.hasColorCompletedAllTrees(Piece.Color.BLUE) || model.hasColorCompletedAllTrees(Piece.Color.GREEN)) {
            showAlert("Cold player has won the game!");
            endGame();
        } else if (model.hasColorCompletedAllTrees(Piece.Color.RED) || model.hasColorCompletedAllTrees(Piece.Color.YELLOW)) {
            showAlert("Warm player has won the game!");
            endGame();
        }
    }

    // Confirms whether the player wants to end the game
    private boolean confirmEndGame() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm End Game");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to manually end the game?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Ends the game after manual confirmation
    private void endGameManually() {
        showAlert("The game has been manually ended.");
        endGame();
    }

    // Ends the game, saves the result, and opens stats
    private void endGame() {
        gameEnded = true;
        isPlayerTurn = false;

        if (turnTimer != null) {
            turnTimer.stop();
        }

        updateScores();
        game.setColdScore(coldColorScore);
        game.setWarmScore(warmColorScore);

        int finalScore = (currentPlayer.getColor() == Piece.Color.RED || currentPlayer.getColor() == Piece.Color.YELLOW)
                ? warmColorScore : coldColorScore;

        boolean scoreSaved = gameDAO.saveScore(currentGameId, currentPlayer.getId(), finalScore);
        boolean endSaved = gameDAO.endGame(currentGameId);

        if (!scoreSaved || !endSaved) {
            showAlert("The game ended, but the result could not be saved.");
        }

        view.getRollButton().setDisable(true);
        view.getEndTurnButton().setDisable(true);
        view.updateTimer(ZERO_VALUE);

        Platform.runLater(() -> {
            GameStatsView statsView = new GameStatsView();
            GameStatsPresenter statsPresenter = new GameStatsPresenter(statsView, currentPlayer, game, gameDAO, movesDAO);
            statsPresenter.initialize();
            statsView.show();
        });
    }

    // Recalculates warm and cold scores from the board
    private void updateScores() {
        warmColorScore = ZERO_VALUE;
        coldColorScore = ZERO_VALUE;

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Stack<Piece> stack = model.getCell(row, col).getStack();
                if (!stack.isEmpty() && stack.peek().getColor() == Piece.Color.BLACK) {
                    Piece below = stack.size() >= 2 ? stack.get(stack.size() - 2) : null;
                    if (below != null) {
                        switch (below.getColor()) {
                            case RED, YELLOW -> warmColorScore++;
                            case BLUE, GREEN -> coldColorScore++;
                        }
                    }
                }
            }
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
        view.refresh();
        updateScores();
        view.updateScore(warmColorScore, coldColorScore);
    }

    // Creates a triangle shape for one piece size
    private Polygon createTriangle(Piece.Size size) {
        double baseSize = switch (size) {
            case SMALL -> SMALL_TRIANGLE_SIZE;
            case MEDIUM -> MEDIUM_TRIANGLE_SIZE;
            case LARGE -> LARGE_TRIANGLE_SIZE;
        };
        double half = baseSize / HALF_DIVISOR;
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
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}