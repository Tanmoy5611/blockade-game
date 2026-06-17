package blockaders.model.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class GameDAO {
    // Returned when no game id is available
    public static final int NO_GAME_ID = -1;

    // Column name for generated game ids
    private static final String GAME_ID_COLUMN = "game_id";

    // Creates a new game row and returns its generated id
    private static final String CREATE_GAME_SQL = """
            INSERT INTO games (start_time, end_time, player_id, color)
            VALUES (?, ?, ?, ?)
            RETURNING game_id
            """;

    // Updates the saved score for one game
    private static final String SAVE_SCORE_SQL = """
            UPDATE games
            SET score = ?
            WHERE game_id = ? AND player_id = ?
            """;

    // Marks a game as ended
    private static final String END_GAME_SQL = """
            UPDATE games
            SET end_time = CURRENT_TIMESTAMP
            WHERE game_id = ?
            """;

    // Finds the latest game that is still open
    private static final String LATEST_UNFINISHED_GAME_SQL = """
            SELECT game_id
            FROM games
            WHERE player_id = ? AND end_time IS NULL
            ORDER BY start_time DESC
            LIMIT 1
            """;

    // Creates a new game or returns the latest unfinished one
    public int createNewGameInDB(int playerId, String colorPreference) {
        int generatedGameId = NO_GAME_ID;

        int unfinished = getLatestUnfinishedGameId(playerId);
        if (unfinished != NO_GAME_ID) {
            return unfinished;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CREATE_GAME_SQL)) {

            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setNull(2, Types.TIMESTAMP);
            stmt.setInt(3, playerId);
            stmt.setString(4, colorPreference);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    generatedGameId = rs.getInt(GAME_ID_COLUMN);
                }
            }

        } catch (SQLException e) {
            System.err.println("Could not create game: " + e.getMessage());
        }

        return generatedGameId;
    }

    // Saves the score for a player in one game
    public boolean saveScore(int gameId, int playerId, int score) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SAVE_SCORE_SQL)) {

            stmt.setInt(1, score);
            stmt.setInt(2, gameId);
            stmt.setInt(3, playerId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Could not save score: " + e.getMessage());
            return false;
        }
    }

    // Sets the end time for a game
    public boolean endGame(int gameId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(END_GAME_SQL)) {

            stmt.setInt(1, gameId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Could not end game: " + e.getMessage());
            return false;
        }
    }

    // Returns the latest unfinished game id for a player
    public static int getLatestUnfinishedGameId(int playerId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(LATEST_UNFINISHED_GAME_SQL)) {

            stmt.setInt(1, playerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(GAME_ID_COLUMN);
                }
            }

        } catch (SQLException e) {
            System.err.println("Could not load unfinished game: " + e.getMessage());
        }

        return NO_GAME_ID;
    }
}