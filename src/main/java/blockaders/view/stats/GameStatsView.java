package blockaders.view.stats;

import blockaders.model.player.PlayerStats;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameStatsView {
    // Game stats window width
    private static final int WINDOW_WIDTH = 1000;

    // Game stats window height
    private static final int WINDOW_HEIGHT = 800;

    // Logo height in pixels
    private static final int LOGO_HEIGHT = 200;

    // Logo width in pixels
    private static final int LOGO_WIDTH = 300;

    // Fixed table row height
    private static final int TABLE_ROW_HEIGHT = 25;

    // Number of stat rows shown
    private static final int TABLE_ROW_COUNT = 3;

    // Extra table header height
    private static final int TABLE_HEADER_HEIGHT = 30;

    // Navigation button width in pixels
    private static final int NAV_BUTTON_WIDTH = 200;

    // Main content box size
    private static final int CONTENT_BOX_SIZE = 400;

    // Navigation bar height
    private static final int NAV_BAR_HEIGHT = 50;

    // Background image percentage size
    private static final int BACKGROUND_SIZE_PERCENT = 80;

    // Shared navigation button style
    private static final String BUTTON_STYLE = """
            -fx-font-size: 18px;
            -fx-padding: 12px 20px;
            -fx-background-color: rgba(87,165,197,0.8);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 4);
            """;

    // Winner label style
    private static final String WINNER_STYLE =
            "-fx-font-size: 20px; -fx-padding: 12px 20px; -fx-background-color: rgba(87,165,197,0.61);"
                    + " -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 10px; -fx-background-radius: 10px;";

    private Stage stage;

    private ImageView gameStatsLogo;
    private TableView<PlayerStats> table;
    private Label winnerLabel;
    private Label playTimeLabel;
    private Button leaderboardButton;
    private Button gameRulesButton;
    private Button homeButton;

    // Creates the game stats view
    public GameStatsView() {
        initializeNodes();
        layoutNodes();
    }

    // Builds labels, table, buttons, and logo
    private void initializeNodes() {
        gameStatsLogo = new ImageView("logo.png");
        gameStatsLogo.setFitHeight(LOGO_HEIGHT);
        gameStatsLogo.setFitWidth(LOGO_WIDTH);

        winnerLabel = new Label();
        winnerLabel.setStyle(WINNER_STYLE);

        playTimeLabel = new Label();
        playTimeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold");

        table = new TableView<>();
        TableColumn<PlayerStats, String> statColumn = new TableColumn<>("Statistic");
        statColumn.setCellValueFactory(param -> param.getValue().statisticProperty());
        table.getColumns().clear();
        table.getColumns().add(statColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(TABLE_ROW_HEIGHT);
        table.setPrefHeight(TABLE_ROW_COUNT * TABLE_ROW_HEIGHT + TABLE_HEADER_HEIGHT);

        leaderboardButton = new Button("See Leaderboard");
        leaderboardButton.setStyle(BUTTON_STYLE);

        gameRulesButton = new Button("Game Rules");
        homeButton = new Button("Home");

        gameRulesButton.setStyle(BUTTON_STYLE);
        homeButton.setStyle(BUTTON_STYLE);

        homeButton.setPrefWidth(NAV_BUTTON_WIDTH);
        gameRulesButton.setPrefWidth(NAV_BUTTON_WIDTH);
    }

    // Places stats content in a new stage
    private void layoutNodes() {
        Image backgroundImage = new Image("background.png");
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BACKGROUND_SIZE_PERCENT, BACKGROUND_SIZE_PERCENT, true, true, true, false)
        );

        VBox logo = new VBox(10, gameStatsLogo);
        logo.setAlignment(Pos.TOP_CENTER);

        VBox vbox = new VBox(20, new Region(), winnerLabel, playTimeLabel, table, leaderboardButton);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle("-fx-padding: 20px;");
        vbox.setPrefWidth(CONTENT_BOX_SIZE);
        vbox.setPrefHeight(CONTENT_BOX_SIZE);

        HBox navBar = new HBox(20, gameRulesButton, homeButton);
        navBar.setAlignment(Pos.CENTER);
        navBar.setStyle("-fx-padding: 10px;");
        navBar.setPrefWidth(CONTENT_BOX_SIZE);
        navBar.setPrefHeight(NAV_BAR_HEIGHT);

        BorderPane root = new BorderPane();
        root.setTop(logo);
        root.setCenter(vbox);
        root.setBottom(navBar);
        root.setBackground(new Background(background));

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(javafx.scene.paint.Color.WHITE);

        this.stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Game Statistics");

        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMaxWidth(WINDOW_WIDTH);
        stage.setMaxHeight(WINDOW_HEIGHT);
    }

    // Shows the stats window
    public void show() {
        if (stage != null) {
            stage.show();
        }
    }

    // Updates winner text
    public void setWinnerLabel(String winner) {
        winnerLabel.setText(winner);
    }

    // Updates play time text
    public void setPlayTimeLabel(String playTime) {
        playTimeLabel.setText(playTime);
    }

    // Updates the stats table rows
    public void setTableData(ObservableList<PlayerStats> stats) {
        table.setItems(stats);
    }

    // Returns the leaderboard button
    public Button getLeaderboardButton() {
        return leaderboardButton;
    }

    // Returns the rules button
    public Button getGameRulesButton() {
        return gameRulesButton;
    }

    // Returns the home button
    public Button getHomeButton() {
        return homeButton;
    }
}