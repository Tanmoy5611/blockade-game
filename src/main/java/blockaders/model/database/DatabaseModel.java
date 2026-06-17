package blockaders.model.database;

import blockaders.model.game.GameStats;
import blockaders.model.leaderboard.LeaderboardModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseModel {
    // Query used for the latest game's stats screen
    private static final String FETCH_GAME_STATS_SQL = """
            SELECT p.player_name AS winner,
                   g.duration AS total_play_time,
                   COUNT(m.move_id) AS total_moves,
                   AVG(m.move_timestamp) AS avg_move_duration,
                   g.score AS total_score
            FROM Games g
            JOIN Players p ON g.player_id = p.player_id
            JOIN Moves m ON g.game_id = m.game_id
            WHERE g.game_id = (SELECT MAX(game_id) FROM Games)
            GROUP BY g.duration, p.player_name, g.score
            """;

    // Query used for the leaderboard table
    private static final String FETCH_LEADERBOARD_SQL = """
            SELECT p.player_name,
                   g.score,
                   COUNT(g.game_id) AS games_played,
                   SUM(CASE WHEN g.player_id = p.player_id THEN 1 ELSE 0 END) AS games_won,
                   SUM(CASE WHEN g.player_id IS NULL THEN 1 ELSE 0 END) AS games_drawn,
                   COUNT(g.game_id)
                       - SUM(CASE WHEN g.player_id = p.player_id THEN 1 ELSE 0 END)
                       - SUM(CASE WHEN g.player_id IS NULL THEN 1 ELSE 0 END) AS games_lost
            FROM Players p
            LEFT JOIN Games g ON p.player_id = g.player_id
            GROUP BY p.player_name, g.score
            ORDER BY g.score DESC
            """;

    // Prevents creating utility class objects
    private DatabaseModel() {
    }

    // Loads stats for the latest game
    public static GameStats fetchGameStats() {
        GameStats stats = new GameStats();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FETCH_GAME_STATS_SQL)) {

            if (resultSet.next()) {
                stats.winner = resultSet.getString("winner");
                stats.totalPlayTime = resultSet.getInt("total_play_time");
                stats.totalMoves = resultSet.getInt("total_moves");
                stats.avgMoveDuration = resultSet.getDouble("avg_move_duration");
                stats.totalScore = resultSet.getInt("total_score");
            }
        } catch (SQLException e) {
            System.err.println("Could not load game stats: " + e.getMessage());
        }
        return stats;
    }

    // Loads all rows for the leaderboard
    public static ObservableList<LeaderboardModel> fetchLeaderboardStats() {
        ObservableList<LeaderboardModel> leaderboard = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FETCH_LEADERBOARD_SQL)) {

            while (resultSet.next()) {
                leaderboard.add(new LeaderboardModel(
                        resultSet.getString("player_name"),
                        resultSet.getInt("score"),
                        resultSet.getInt("games_played"),
                        resultSet.getInt("games_won"),
                        resultSet.getInt("games_lost"),
                        resultSet.getInt("games_drawn")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Could not load leaderboard: " + e.getMessage());
        }
        return leaderboard;
    }
}