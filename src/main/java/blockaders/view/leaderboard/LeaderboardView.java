package blockaders.view.leaderboard;

import blockaders.model.leaderboard.LeaderboardModel;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LeaderboardView {
    // Leaderboard window width
    private static final int WINDOW_WIDTH = 1000;

    // Leaderboard window height
    private static final int WINDOW_HEIGHT = 800;

    // Top logo width in pixels
    private static final int LOGO_WIDTH = 450;

    // Top logo height in pixels
    private static final int LOGO_HEIGHT = 300;

    // Table column width in pixels
    private static final int COLUMN_WIDTH = 100;

    // Navigation button width in pixels
    private static final int NAV_BUTTON_WIDTH = 200;

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

    private TableView<LeaderboardModel> leaderboardTable;
    private Button homeButton;
    private Button gameRulesButton;
    private ImageView logo;

    // Creates the leaderboard view
    public LeaderboardView() {
        initializeNodes();
    }

    // Builds the leaderboard table, buttons, and logo
    private void initializeNodes() {
        logo = new ImageView("Leaderboard.png");
        logo.setFitWidth(LOGO_WIDTH);
        logo.setFitHeight(LOGO_HEIGHT);

        leaderboardTable = new TableView<>();
        createTableColumns();

        homeButton = new Button("Home");
        gameRulesButton = new Button("Game Rules");

        styleButtons();
    }

    // Creates all leaderboard table columns
    private void createTableColumns() {
        TableColumn<LeaderboardModel, String> playerNameColumn = new TableColumn<>("Player Name");
        playerNameColumn.setCellValueFactory(param -> param.getValue().playerNameProperty());
        playerNameColumn.setCellFactory(column -> createCenteredCell());
        playerNameColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<LeaderboardModel, Integer> totalScoreColumn = new TableColumn<>("Total Score");
        totalScoreColumn.setCellValueFactory(param -> param.getValue().totalScoreProperty().asObject());
        totalScoreColumn.setCellFactory(column -> createCenteredCell());
        totalScoreColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<LeaderboardModel, Integer> gamesPlayedColumn = new TableColumn<>("Games Played");
        gamesPlayedColumn.setCellValueFactory(param -> param.getValue().gamesPlayedProperty().asObject());
        gamesPlayedColumn.setCellFactory(column -> createCenteredCell());
        gamesPlayedColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<LeaderboardModel, Integer> gamesWonColumn = new TableColumn<>("Games Won");
        gamesWonColumn.setCellValueFactory(param -> param.getValue().gamesWonProperty().asObject());
        gamesWonColumn.setCellFactory(column -> createCenteredCell());
        gamesWonColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<LeaderboardModel, Integer> gamesLostColumn = new TableColumn<>("Games Lost");
        gamesLostColumn.setCellValueFactory(param -> param.getValue().gamesLostProperty().asObject());
        gamesLostColumn.setCellFactory(column -> createCenteredCell());
        gamesLostColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<LeaderboardModel, Integer> gamesDrawnColumn = new TableColumn<>("Games Drawn");
        gamesDrawnColumn.setCellValueFactory(param -> param.getValue().gamesDrawnProperty().asObject());
        gamesDrawnColumn.setCellFactory(column -> createCenteredCell());
        gamesDrawnColumn.setPrefWidth(COLUMN_WIDTH);

        leaderboardTable.getColumns().add(playerNameColumn);
        leaderboardTable.getColumns().add(totalScoreColumn);
        leaderboardTable.getColumns().add(gamesPlayedColumn);
        leaderboardTable.getColumns().add(gamesWonColumn);
        leaderboardTable.getColumns().add(gamesLostColumn);
        leaderboardTable.getColumns().add(gamesDrawnColumn);
    }

    // Applies shared button styles
    private void styleButtons() {
        homeButton.setStyle(BUTTON_STYLE);
        gameRulesButton.setStyle(BUTTON_STYLE);
    }

    // Opens the leaderboard in a new window
    public void showLeaderboard() {
        VBox vbox = new VBox(20, logo);
        vbox.setAlignment(Pos.CENTER);

        VBox vbox1 = new VBox(20, leaderboardTable);
        vbox1.setStyle("-fx-padding: 20px;");

        HBox navBar = new HBox(20, gameRulesButton, homeButton);
        navBar.setAlignment(Pos.CENTER);
        navBar.setStyle("-fx-padding: 10px;");

        BorderPane leaderboard = new BorderPane();
        leaderboard.setStyle("-fx-background-color: #ffffff;");
        leaderboard.setTop(vbox);
        leaderboard.setCenter(vbox1);
        leaderboard.setBottom(navBar);

        Scene scene = new Scene(leaderboard, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(javafx.scene.paint.Color.WHITE);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Leaderboard");
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMaxWidth(WINDOW_WIDTH);
        stage.setMaxHeight(WINDOW_HEIGHT);

        homeButton.setPrefWidth(NAV_BUTTON_WIDTH);
        gameRulesButton.setPrefWidth(NAV_BUTTON_WIDTH);

        stage.show();
    }

    // Updates the table rows
    public void updateLeaderboardTable(ObservableList<LeaderboardModel> leaderboardStats) {
        leaderboardTable.setItems(leaderboardStats);
    }

    // Creates a centered table cell
    private <T> TableCell<LeaderboardModel, T> createCenteredCell() {
        return new TableCell<>() {
            @Override
            // Updates one centered table cell
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: center;");
                }
            }
        };
    }

    // Returns the home button
    public Button getHomeButton() {
        return homeButton;
    }

    // Returns the rules button
    public Button getGameRulesButton() {
        return gameRulesButton;
    }
}