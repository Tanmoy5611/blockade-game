package blockaders.model.board;

import blockaders.model.database.PieceDAO;

import java.util.Stack;

public class Board {
    // Board is always a 5 by 5 grid
    public static final int SIZE = 5;

    // Each color owns five trees in the starting layout
    public static final int TREES_PER_COLOR = 5;

    // A normal tree is large, medium, small
    private static final int TREE_STACK_SIZE = 3;

    // A completed tree has a black blockade piece on top
    private static final int BLOCKADED_TREE_STACK_SIZE = 4;

    // Five medium pieces start in each corner supply
    private static final int MEDIUM_PIECES_PER_CORNER = 5;

    // First index on the board
    private static final int FIRST_INDEX = 0;

    // Last index on the board
    private static final int LAST_INDEX = SIZE - 1;

    // Top piece offset from the end of the stack
    private static final int TOP_OFFSET = 1;

    // Piece under the top piece
    private static final int SECOND_FROM_TOP_OFFSET = 2;

    // Third piece from the top
    private static final int THIRD_FROM_TOP_OFFSET = 3;

    // Fourth piece from the top
    private static final int FOURTH_FROM_TOP_OFFSET = 4;

    // Returned when a move cannot be made
    private static final int INVALID_MOVE_COST = -1;

    private final Position[][] grid;

    // Creates the board positions and places the starting pieces
    public Board() {
        grid = new Position[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = new Position(i, j);
            }
        }
        setupBoard();
        saveAllPiecesToDB();
    }

    // Places all starting pieces according to the Blockade board setup
    private void setupBoard() {
        Piece.Color[][] pattern = {
                { Piece.Color.CLEAR, Piece.Color.BLUE, Piece.Color.BLUE, Piece.Color.BLUE, Piece.Color.CLEAR },
                { Piece.Color.RED, Piece.Color.RED, Piece.Color.RED, Piece.Color.BLUE, Piece.Color.YELLOW },
                { Piece.Color.RED, Piece.Color.GREEN, Piece.Color.CLEAR, Piece.Color.BLUE, Piece.Color.YELLOW },
                { Piece.Color.RED, Piece.Color.GREEN, Piece.Color.YELLOW, Piece.Color.YELLOW, Piece.Color.YELLOW },
                { Piece.Color.CLEAR, Piece.Color.GREEN, Piece.Color.GREEN, Piece.Color.GREEN, Piece.Color.CLEAR }
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece.Color color = pattern[row][col];
                grid[row][col].placePiece(new Piece(color, Piece.Size.LARGE));
            }
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece top = grid[row][col].getStack().peek();
                if (top.getColor() != Piece.Color.CLEAR) {
                    grid[row][col].placePiece(new Piece(top.getColor(), Piece.Size.SMALL));
                }
            }
        }

        placeMediumPiecesInCorner(FIRST_INDEX, FIRST_INDEX, Piece.Color.GREEN);
        placeMediumPiecesInCorner(FIRST_INDEX, LAST_INDEX, Piece.Color.RED);
        placeMediumPiecesInCorner(LAST_INDEX, FIRST_INDEX, Piece.Color.YELLOW);
        placeMediumPiecesInCorner(LAST_INDEX, LAST_INDEX, Piece.Color.BLUE);
    }

    // Places the corner stack of medium pieces for one color
    private void placeMediumPiecesInCorner(int row, int col, Piece.Color color) {
        for (int i = 0; i < MEDIUM_PIECES_PER_CORNER; i++) {
            grid[row][col].placePiece(new Piece(color, Piece.Size.MEDIUM));
        }
    }

    // Returns the board position at the given row and column
    public Position getCell(int row, int col) {
        return grid[row][col];
    }

    // Checks if a row and column are inside the board
    public boolean isInside(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    // Checks if a move follows the allowed straight-line movement paths
    public boolean isValidMove(Position from, Position to, Piece movingPiece) {
        int dx = to.getRow() - from.getRow();
        int dy = to.getCol() - from.getCol();

        if (dx == 0 && dy == 0) {
            return false;
        }

        boolean isStraightLine = dx == 0 || dy == 0 || Math.abs(dx) == Math.abs(dy);
        if (!isStraightLine) {
            return false;
        }

        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        int stepRow = Integer.signum(dx);
        int stepCol = Integer.signum(dy);
        int currRow = from.getRow();
        int currCol = from.getCol();

        for (int i = 1; i < steps; i++) {
            currRow += stepRow;
            currCol += stepCol;
            Position intermediate = getCell(currRow, currCol);

            if (intermediate.getStack().isEmpty()) {
                continue;
            }

            Piece topPiece = intermediate.getStack().peek();
            if (topPiece.getColor() == Piece.Color.BLACK && isOpponentColor(intermediate, movingPiece.getColor())) {
                return false;
            }
        }

        return true;
    }

    // Calculates movement cost with the default path rule
    public int calculateMoveCost(Position from, Position to, Piece movingPiece) {
        return calculateMoveCost(from, to, movingPiece, false);
    }

    // Calculates movement cost and optionally blocks paths through empty cells
    public int calculateMoveCost(Position from, Position to, Piece movingPiece, boolean requireOccupiedIntermediateCells) {
        int dx = to.getRow() - from.getRow();
        int dy = to.getCol() - from.getCol();
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        if (steps == 0) {
            return INVALID_MOVE_COST;
        }

        int stepRow = Integer.signum(dx);
        int stepCol = Integer.signum(dy);
        int currRow = from.getRow();
        int currCol = from.getCol();
        int cost = 0;

        for (int i = 1; i <= steps; i++) {
            currRow += stepRow;
            currCol += stepCol;
            Position intermediate = getCell(currRow, currCol);

            if (i < steps && requireOccupiedIntermediateCells && intermediate.getStack().isEmpty()) {
                return INVALID_MOVE_COST;
            }

            if (i == steps) {
                if (!canLandOn(intermediate, movingPiece)) {
                    return INVALID_MOVE_COST;
                }
            }

            cost++;
        }

        return cost;
    }

    // Checks whether a moving piece can land on the target position
    public boolean canLandOn(Position to, Piece movingPiece) {
        if (to.getStack().isEmpty()) {
            return true;
        }

        Piece top = to.getStack().peek();

        if (top.getColor() == Piece.Color.CLEAR) {
            return true;
        }

        if (top.getColor() != movingPiece.getColor()) {
            return false;
        }

        if (top.getSize().ordinal() >= movingPiece.getSize().ordinal()) {
            return !(movingPiece.getSize() == Piece.Size.MEDIUM && top.getSize() == Piece.Size.SMALL);
        }

        return false;
    }

    // Checks if the position belongs to an opponent color
    private boolean isOpponentColor(Position position, Piece.Color playerColor) {
        if (position.getStack().isEmpty()) {
            return false;
        }

        Piece topPiece = position.getStack().peek();
        if (topPiece.getColor() != Piece.Color.BLACK) {
            return topPiece.getColor() != playerColor;
        }

        if (position.getStack().size() < 2) {
            return false;
        }

        Piece underTop = position.getStack().get(position.getStack().size() - SECOND_FROM_TOP_OFFSET);
        return underTop.getColor() != playerColor;
    }

    // Turns a completed tree into a blockaded tree and returns the tree color
    public Piece.Color checkAndBlockTree(int row, int col) {
        Stack<Piece> stack = grid[row][col].getStack();
        if (stack.isEmpty() || stack.peek().getColor() == Piece.Color.BLACK || stack.size() < TREE_STACK_SIZE) {
            return null;
        }

        Piece small = stack.get(stack.size() - TOP_OFFSET);
        Piece medium = stack.get(stack.size() - SECOND_FROM_TOP_OFFSET);
        Piece large = stack.get(stack.size() - THIRD_FROM_TOP_OFFSET);

        if (small.getSize() == Piece.Size.SMALL &&
                medium.getSize() == Piece.Size.MEDIUM &&
                large.getSize() == Piece.Size.LARGE &&
                small.getColor() == medium.getColor() &&
                medium.getColor() == large.getColor()) {

            grid[row][col].placePiece(new Piece(Piece.Color.BLACK, Piece.Size.LARGE));
            return small.getColor();
        }

        return null;
    }

    // Counts completed trees for the warm colors
    public int getWarmScore() {
        return countCompletedTrees(Piece.Color.RED, Piece.Color.YELLOW);
    }

    // Counts completed trees for the cold colors
    public int getColdScore() {
        return countCompletedTrees(Piece.Color.BLUE, Piece.Color.GREEN);
    }

    // Counts completed trees for two colors
    private int countCompletedTrees(Piece.Color firstColor, Piece.Color secondColor) {
        int completedTrees = 0;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece.Color color = getBlockadedTreeColor(row, col);
                if (color == firstColor || color == secondColor) {
                    completedTrees++;
                }
            }
        }

        return completedTrees;
    }

    // Checks whether one color has completed all its trees
    public boolean hasColorCompletedAllTrees(Piece.Color color) {
        int completedTrees = 0;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (getBlockadedTreeColor(row, col) == color) {
                    completedTrees++;
                }
            }
        }

        return completedTrees == TREES_PER_COLOR;
    }

    // Checks whether the stack at one position is a completed tree
    public boolean isTree(int row, int col) {
        Stack<Piece> stack = grid[row][col].getStack();
        if (stack.size() < TREE_STACK_SIZE) return false;

        Piece small = stack.get(stack.size() - TOP_OFFSET);
        Piece medium = stack.get(stack.size() - SECOND_FROM_TOP_OFFSET);
        Piece large = stack.get(stack.size() - THIRD_FROM_TOP_OFFSET);

        return small.getSize() == Piece.Size.SMALL &&
                medium.getSize() == Piece.Size.MEDIUM &&
                large.getSize() == Piece.Size.LARGE &&
                small.getColor() == medium.getColor() &&
                medium.getColor() == large.getColor();
    }

    // Returns the color of a blockaded tree or null if there is none
    public Piece.Color getBlockadedTreeColor(int row, int col) {
        Stack<Piece> stack = grid[row][col].getStack();
        if (stack.size() < BLOCKADED_TREE_STACK_SIZE || stack.peek().getColor() != Piece.Color.BLACK) {
            return null;
        }

        Piece small = stack.get(stack.size() - SECOND_FROM_TOP_OFFSET);
        Piece medium = stack.get(stack.size() - THIRD_FROM_TOP_OFFSET);
        Piece large = stack.get(stack.size() - FOURTH_FROM_TOP_OFFSET);

        boolean isTree = small.getSize() == Piece.Size.SMALL
                && medium.getSize() == Piece.Size.MEDIUM
                && large.getSize() == Piece.Size.LARGE
                && small.getColor() == medium.getColor()
                && medium.getColor() == large.getColor();

        return isTree ? small.getColor() : null;
    }

    // Saves all board pieces to the database when possible
    private void saveAllPiecesToDB() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Stack<Piece> stack = grid[row][col].getStack();
                for (Piece piece : stack) {
                    savePieceIfNeeded(piece);
                }
            }
        }
    }

    // Saves a piece only if it does not already have a database id
    private void savePieceIfNeeded(Piece piece) {
        if (piece.getPieceId() > 0) {
            return;
        }

        try {
            PieceDAO.savePiece(piece);
        } catch (Exception ignored) {
            // Login and setup show database errors when configuration is missing
        }
    }
}