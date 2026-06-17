package blockaders.model.database;

import blockaders.model.player.HumanPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDAO {
    // Column index used for count queries
    private static final int COUNT_COLUMN_INDEX = 1;

    // Loads a player and their best saved score by username
    private static final String LOGIN_SQL = """
            SELECT p.player_id, p.password, COALESCE(MAX(g.score), 0) AS score
            FROM players p
            LEFT JOIN games g ON p.player_id = g.player_id
            WHERE p.player_name = ?
            GROUP BY p.player_id, p.password
            """;

    // Checks whether a username already exists
    private static final String USER_EXISTS_SQL = """
            SELECT COUNT(*)
            FROM players
            WHERE player_name = ?
            """;

    // Inserts a new player
    private static final String REGISTER_SQL = """
            INSERT INTO players (player_name, password)
            VALUES (?, ?)
            """;

    // Logs a player in when the username and password match
    public HumanPlayer login(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(LOGIN_SQL)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && password.equals(rs.getString("password"))) {
                    return new HumanPlayer(
                            rs.getInt("player_id"),
                            username,
                            rs.getInt("score")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Could not log in player: " + e.getMessage());
        }

        return null;
    }

    // Registers a new player when the username is available
    public boolean register(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement checkStmt = conn.prepareStatement(USER_EXISTS_SQL)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(COUNT_COLUMN_INDEX) > 0) {
                        return false;
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(REGISTER_SQL)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Could not register player: " + e.getMessage());
            return false;
        }
    }
}