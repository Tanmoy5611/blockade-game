package blockaders.model.board;

import java.util.Stack;

public class Position {
    private final int row;
    private final int col;
    private final Stack<Piece> stack;

    // Creates one board position with an empty piece stack
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
        this.stack = new Stack<>();
    }

    // Returns the row index
    public int getRow() {
        return row;
    }

    // Returns the column index
    public int getCol() {
        return col;
    }

    // Returns the stack of pieces on this position
    public Stack<Piece> getStack() {
        return stack;
    }

    // Places a piece on top of the stack
    public void placePiece(Piece piece) {
        stack.push(piece);
    }

    // Removes and returns the top piece
    public Piece removeTopPiece() {
        return stack.isEmpty() ? null : stack.pop();
    }
}