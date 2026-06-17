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

public class BoardColdView extends VBox {
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

    // Default cold board cell style
    private static final String CELL_STYLE =
            "-fx-border-color: #333; -fx-border-width: 3.5px; -fx-background-color: white; -fx-border-radius: 5px; -fx-background-radius: 5px;";

    private final StackPane[][] cellNodes;
    private final Button rollButton;
    private final Button endTurnButton;
    private final Button endGameButton;
    private final Text blueDieValue;
    private final Text greenDieValue;
    private final Text coldColorScoreText;
    private final Text warmColorScoreText;
    private final Text timerText;
    private final GridPane boardGrid;
    private final ImageView gameStatsLogo;
    private final Board board;

    // Creates the cold board view
    public BoardColdView(Board board) {
        this.board = board;
        setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        cellNodes = new StackPane[Board.SIZE][Board.SIZE];

        gameStatsLogo = new ImageView("logo.png");
        gameStatsLogo.setFitHeight(LOGO_HEIGHT);
        gameStatsLogo.setFitWidth(LOGO_WIDTH);
        gameStatsLogo.setPreserveRatio(true);

        rollButton = createStyledButton("Roll Dice");
        endTurnButton = createStyledButton("End Turn");
        endGameButton = createStyledButton("End Game", "rgba(87,87,197,0.8)");


        blueDieValue = createStyledCounter("Blue: 0", "#4FC3F7");
        greenDieValue = createStyledCounter("Green: 0", "#81C784");

        coldColorScoreText = createStyledLabel("Cold Score: 0", "#4FC3F7");
        warmColorScoreText = createStyledLabel("Warm Score: 0", "#FF8A65");
        timerText = createStyledLabel("Time: 0s", "#424242");

        boardGrid = new GridPane();
        boardGrid.setHgap(5);
        boardGrid.setVgap(5);
        boardGrid.setAlignment(Pos.CENTER);

        initializeCells();
        layoutNodes();
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

    // Creates all board cell nodes
    private void initializeCells() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                StackPane cellPane = new StackPane();
                cellPane.setPrefSize(CELL_SIZE, CELL_SIZE);
                cellPane.setStyle(CELL_STYLE);
                cellNodes[row][col] = cellPane;
                boardGrid.add(cellPane, col, row);
            }
        }
    }

    // Places all controls on the cold board screen
    private void layoutNodes() {
        setSpacing(20);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #e3f2fd;");

        getChildren().add(gameStatsLogo);

        HBox buttonsBox = new HBox(25, rollButton, endTurnButton, endGameButton);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox diceValuesBox = new VBox(15, blueDieValue, greenDieValue);
        diceValuesBox.setAlignment(Pos.CENTER);

        VBox controlBox = new VBox(20, buttonsBox, diceValuesBox, timerText);
        controlBox.setAlignment(Pos.CENTER);

        HBox scoreBox = new HBox(50, coldColorScoreText, warmColorScoreText);
        scoreBox.setAlignment(Pos.CENTER);

        getChildren().addAll(boardGrid, controlBox, scoreBox);
    }

    // Creates a styled button with the default color
    private Button createStyledButton(String text) {
        return createStyledButton(text, "rgba(87,165,197,0.8)");
    }

    // Creates a styled button with a custom background color
    private Button createStyledButton(String text, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle(String.format("""
            -fx-font-size: 15px;
            -fx-padding: 10px 15px;
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
        """, backgroundColor));
        return button;
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

    // Updates score labels
    public void updateScore(int warmScore, int coldScore) {
        warmColorScoreText.setText("Warm Score: " + warmScore);
        coldColorScoreText.setText("Cold Score: " + coldScore);
    }

    // Updates the timer label
    public void updateTimer(int seconds) {
        timerText.setText("Time: " + seconds + "s");
    }

    // Returns the roll button
    public Button getRollButton() {
        return rollButton;
    }

    // Returns the end turn button
    public Button getEndTurnButton() {
        return endTurnButton;
    }

    // Returns the end game button
    public Button getEndGameButton() {
        return endGameButton;
    }

    // Returns a board cell node
    public StackPane getCellNode(int row, int col) {
        return cellNodes[row][col];
    }

    // Updates cold dice values
    public void setDiceValues(int blue, int green) {
        blueDieValue.setText("Blue: " + blue);
        greenDieValue.setText("Green: " + green);
    }
}