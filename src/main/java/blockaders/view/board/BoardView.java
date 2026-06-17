package blockaders.view.board;

import blockaders.model.board.Board;
import blockaders.model.board.Piece;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Stack;

public class BoardView extends VBox {
    // Main board screen width
    private static final int WINDOW_WIDTH = 1000;

    // Main board screen height
    private static final int WINDOW_HEIGHT = 800;

    // Logo height in pixels
    private static final int LOGO_HEIGHT = 200;

    // Logo width in pixels
    private static final int LOGO_WIDTH = 210;

    // Cell size in pixels
    private static final int CELL_SIZE = 80;

    // Vertical stack offset for pieces in one cell
    private static final int PIECE_STACK_OFFSET = 5;

    // Small piece radius
    private static final int SMALL_RADIUS = 12;

    // Medium piece radius
    private static final int MEDIUM_RADIUS = 18;

    // Large piece radius
    private static final int LARGE_RADIUS = 24;

    // Default warm board cell style
    private static final String CELL_STYLE =
            "-fx-border-color: #555; -fx-border-width: 3.5px; -fx-background-color: white; -fx-border-radius: 5px; -fx-background-radius: 5px;";

    // Default button style
    private static final String BUTTON_STYLE = """
            -fx-font-size: 15px;
            -fx-padding: 10px 15px;
            -fx-background-color: rgba(87,165,197,0.8);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
            """;

    // End game button style
    private static final String END_GAME_BUTTON_STYLE = """
            -fx-font-size: 15px;
            -fx-padding: 10px 15px;
            -fx-background-color: rgba(197,87,87,0.8);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
            """;

    private StackPane[][] cellNodes;
    private Button rollButton;
    private Button endTurnButton;
    private Text redDieValue;
    private Text yellowDieValue;
    private Text warmColorScoreText;
    private Text coldColorScoreText;
    private Text timerText;
    private ImageView gameStatsLogo;
    private Button endGameButton;
    private GridPane boardGrid;
    private final Board board;

    // Creates the warm board view
    public BoardView(Board board) {
        this.board = board;
        setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        initialiseNodes();
        layoutNodes();
    }

    // Builds controls, counters, logo, and board cells
    private void initialiseNodes() {
        gameStatsLogo = new ImageView("logo.png");
        gameStatsLogo.setFitHeight(LOGO_HEIGHT);
        gameStatsLogo.setFitWidth(LOGO_WIDTH);
        gameStatsLogo.setPreserveRatio(true);

        boardGrid = new GridPane();
        cellNodes = new StackPane[Board.SIZE][Board.SIZE];

        rollButton = new Button("Roll Dice");
        endTurnButton = new Button("End Turn");

        redDieValue = createStyledCounter("Red: 0", "#FF5252");
        yellowDieValue = createStyledCounter("Yellow: 0", "#FFD600");

        warmColorScoreText = createStyledLabel("Warm Score: 0", "#FF8A65");
        coldColorScoreText = createStyledLabel("Cold Score: 0", "#4FC3F7");
        timerText = createStyledLabel("Time: 0s", "#424242");

        endGameButton = new Button("End Game");
        endGameButton.setStyle(END_GAME_BUTTON_STYLE);

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                StackPane cellPane = new StackPane();
                cellPane.setPrefSize(CELL_SIZE, CELL_SIZE);
                cellPane.setStyle(CELL_STYLE);
                cellNodes[row][col] = cellPane;
            }
        }

        rollButton.setStyle(BUTTON_STYLE);
        endTurnButton.setStyle(BUTTON_STYLE);
    }

    // Places all controls on the warm board screen
    private void layoutNodes() {
        setSpacing(10);
        setPadding(new Insets(15));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #f0f8ff;");

        getChildren().add(gameStatsLogo);

        boardGrid.setHgap(5);
        boardGrid.setVgap(5);
        boardGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                boardGrid.add(cellNodes[row][col], col, row);
            }
        }

        HBox buttonsBox = new HBox(15, rollButton, endTurnButton, endGameButton);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10));

        VBox diceValuesBox = new VBox(10, redDieValue, yellowDieValue);
        diceValuesBox.setAlignment(Pos.CENTER);

        VBox diceBox = new VBox(15, buttonsBox, diceValuesBox, timerText);
        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(10));

        HBox scoreBox = new HBox(30, warmColorScoreText, coldColorScoreText);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setPadding(new Insets(10));

        getChildren().addAll(boardGrid, diceBox, scoreBox);
    }

    // Updates score labels
    public void updateScore(int warmScore, int coldScore) {
        warmColorScoreText.setText("Warm Score: " + warmScore);
        coldColorScoreText.setText("Cold Score: " + coldScore);
    }

    // Updates the timer label
    public void updateTimer(int secondsRemaining) {
        timerText.setText("Time: " + secondsRemaining + "s");
    }

    // Redraws the piece circles in every cell
    public void refresh() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                StackPane cell = cellNodes[row][col];
                cell.getChildren().clear();

                Stack<Piece> stack = board.getCell(row, col).getStack();

                int i = 0;
                for (Piece piece : stack) {
                    Circle circle = createPieceCircle(piece);
                    circle.setTranslateY(-i * PIECE_STACK_OFFSET);
                    cell.getChildren().add(circle);
                    i++;
                }
            }
        }
    }

    // Creates a circle for one board piece
    private Circle createPieceCircle(Piece piece) {
        double radius = switch (piece.getSize()) {
            case SMALL -> SMALL_RADIUS;
            case MEDIUM -> MEDIUM_RADIUS;
            case LARGE -> LARGE_RADIUS;
        };

        Circle circle = new Circle(radius);
        circle.setFill(mapColor(piece.getColor()));
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1.5);
        return circle;
    }

    // Creates a styled label for score and time text
    private Text createStyledLabel(String text, String color) {
        Text label = new Text(text);
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 20));
        label.setFill(Color.web(color));
        label.setStroke(Color.web("#222222"));
        label.setStrokeWidth(0.3);
        label.setEffect(new DropShadow(2, Color.gray(0, 0.3)));
        return label;
    }

    // Creates a styled dice counter
    private Text createStyledCounter(String text, String color) {
        Text label = new Text(text);
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        label.setFill(Color.web(color));
        label.setEffect(new DropShadow(1.5, Color.gray(0, 0.25)));
        return label;
    }

    // Maps game colors to JavaFX colors
    private Color mapColor(Piece.Color color) {
        return switch (color) {
            case RED -> Color.RED;
            case GREEN -> Color.LIMEGREEN;
            case BLUE -> Color.DODGERBLUE;
            case YELLOW -> Color.GOLD;
            case CLEAR -> Color.LIGHTGRAY;
            case BLACK -> Color.DIMGRAY;
        };
    }

    // Returns the end game button
    public Button getEndGameButton() {
        return endGameButton;
    }

    // Returns the roll button
    public Button getRollButton() {
        return rollButton;
    }

    // Returns the end turn button
    public Button getEndTurnButton() {
        return endTurnButton;
    }

    // Updates warm dice values
    public void setDiceValues(int redValue, int yellowValue) {
        redDieValue.setText("Red: " + redValue);
        yellowDieValue.setText("Yellow: " + yellowValue);
    }

    // Returns a board cell node
    public StackPane getCellNode(int row, int col) {
        return cellNodes[row][col];
    }
}