package blockaders.model.database;

import blockaders.model.board.Piece;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PieceDAO {
    // Column name for generated piece ids
    private static final String PIECE_ID_COLUMN = "piece_id";

    // Saves one piece and returns its generated id
    private static final String SAVE_PIECE_SQL = """
            INSERT INTO pieces (color, size)
            VALUES (?, ?)
            RETURNING piece_id
            """;

    // Saves one piece and stores its generated id on the piece object
    public static int savePiece(Piece piece) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SAVE_PIECE_SQL)) {

            stmt.setString(1, piece.getColor().name());
            stmt.setString(2, piece.getSize().name());

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(PIECE_ID_COLUMN);
                    piece.setPieceId(generatedId);
                    return generatedId;
                }
            }
        }

        throw new SQLException("Could not get the generated piece id");
    }
}