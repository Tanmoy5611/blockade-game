package blockaders.model.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MovesDAO {
    // Saves one move row
    private static final String SAVE_MOVE_SQL = """
            INSERT INTO moves (game_id, move_timestamp, from_row, from_col, to_row, to_col, piece_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    // Saves one move made during a game
    public void saveMove(int gameId, int playerId, int timeElapsed, int fromRow, int fromCol, int toRow, int toCol, int pieceId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SAVE_MOVE_SQL)) {

            stmt.setInt(1, gameId);
            stmt.setInt(2, timeElapsed);
            stmt.setInt(3, fromRow);
            stmt.setInt(4, fromCol);
            stmt.setInt(5, toRow);
            stmt.setInt(6, toCol);
            stmt.setInt(7, pieceId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not save move: " + e.getMessage());
        }
    }
}