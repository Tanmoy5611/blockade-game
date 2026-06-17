package blockaders.model.board;

public class Piece {
    // Database id used before a piece is saved
    private static final int UNSAVED_PIECE_ID = -1;

    public enum Color { RED, GREEN, BLUE, YELLOW, BLACK, CLEAR }
    public enum Size { SMALL, MEDIUM, LARGE }

    private final Color color;
    private final Size size;
    private int pieceId = UNSAVED_PIECE_ID;

    // Creates a piece with a color and size
    public Piece(Color color, Size size) {
        this.color = color;
        this.size = size;
    }

    // Returns the piece color
    public Color getColor() {
        return color;
    }

    // Returns the piece size
    public Size getSize() {
        return size;
    }

    // Returns the database id for this piece
    public int getPieceId() {
        return pieceId;
    }

    // Updates the database id after saving the piece
    public void setPieceId(int pieceId) {
        this.pieceId = pieceId;
    }
}