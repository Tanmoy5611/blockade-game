package blockaders.model.player;

import blockaders.model.board.Board;
import blockaders.model.board.Piece;
import blockaders.model.board.Position;
import blockaders.model.game.Dice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Bot {
    // Chance that the bot tries a tree-focused action
    private static final double TREE_ACTION_CHANCE = 0.20;

    // Number of pieces needed before a tree is almost complete
    private static final int NEAR_TREE_SIZE_COUNT = 2;

    // Number of pieces needed for a full tree
    private static final int COMPLETE_TREE_SIZE_COUNT = 3;

    // Highest stack size before a position is blocked for bot movement
    private static final int MAX_STACK_SIZE_FOR_MOVE = 3;

    // Score used when a move has no tree value
    private static final int NO_TREE_POTENTIAL = -1;

    // Bonus for completing a tree shape
    private static final int COMPLETE_TREE_BONUS = 20;

    // Bonus for improving a future tree
    private static final int PARTIAL_TREE_BONUS = 5;

    // Weight used for nearby friendly pieces
    private static final int NEIGHBOR_SCORE_WEIGHT = 2;

    // All directions the bot checks around a position
    private static final int[][] DIRECTIONS = {
            {0, 1}, {0, -1}, {1, 0}, {-1, 0},
            {1, 1}, {-1, -1}, {-1, 1}, {1, -1}
    };

    private final Board model;
    private final List<Piece.Color> controlledColors = new ArrayList<>();
    private final Map<Piece.Color, Dice> diceMap = new HashMap<>();
    private final Random random = new Random();

    // Creates a bot that controls the opposite temperature group
    public Bot(Board model, Piece.Color player1, Piece.Color player2) {
        this.model = model;
        assignOppositeColors(player1, player2);
        controlledColors.forEach(color -> diceMap.put(color, new Dice(color.name().toLowerCase())));
    }

    // Assigns cold colors when the human is warm and warm colors when the human is cold
    private void assignOppositeColors(Piece.Color firstColor, Piece.Color secondColor) {
        boolean humanControlsWarm = isWarmColor(firstColor) || isWarmColor(secondColor);
        controlledColors.addAll(humanControlsWarm
                ? List.of(Piece.Color.BLUE, Piece.Color.GREEN)
                : List.of(Piece.Color.RED, Piece.Color.YELLOW));
    }

    // Plays one full bot turn
    public void playFullTurn() {
        Map<Piece.Color, Integer> moves = rollDiceForControlledColors();

        if (moves.values().stream().allMatch(points -> points == 0)) {
            return;
        }

        executeMoves(moves);
    }

    // Rolls dice for each color controlled by the bot
    private Map<Piece.Color, Integer> rollDiceForControlledColors() {
        Map<Piece.Color, Integer> moves = new HashMap<>();
        controlledColors.forEach(color -> moves.put(color, diceMap.get(color).roll()));
        return moves;
    }

    // Uses available movement points in priority order
    private void executeMoves(Map<Piece.Color, Integer> moves) {
        while (true) {
            boolean moved = false;

            for (Piece.Color color : controlledColors) {
                int remainingMoves = moves.getOrDefault(color, 0);
                if (remainingMoves <= 0) {
                    continue;
                }

                if (tryCompleteTree(color) || prepareTree(color) || smartMove(color) || blockOpponent(color)) {
                    moves.put(color, remainingMoves - 1);
                    moved = true;
                    break;
                }
            }

            if (!moved) {
                break;
            }
        }
    }

    // Tries to finish a tree by moving a medium piece under a small piece
    private boolean tryCompleteTree(Piece.Color color) {
        if (!shouldTryTreeAction()) {
            return false;
        }

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Position position = model.getCell(row, col);
                if (position.getStack().isEmpty()) {
                    continue;
                }

                Piece top = position.getStack().peek();
                if (top.getColor() == color && top.getSize() == Piece.Size.SMALL && isOneMoveFromTree(row, col, color)) {
                    Position mediumSource = findMovablePiece(color, Piece.Size.MEDIUM, row, col);
                    if (mediumSource != null && isLegalMove(mediumSource, position, color)) {
                        Piece small = position.removeTopPiece();
                        position.placePiece(mediumSource.removeTopPiece());
                        position.placePiece(small);
                        model.checkAndBlockTree(row, col);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Tries to add a missing size to a two-piece tree setup
    private boolean prepareTree(Piece.Color color) {
        if (!shouldTryTreeAction()) {
            return false;
        }

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Position position = model.getCell(row, col);
                if (model.isTree(row, col) || position.getStack().size() < NEAR_TREE_SIZE_COUNT) {
                    continue;
                }

                List<Piece.Size> sizes = getMatchingSizes(position, color);
                if (sizes.size() == NEAR_TREE_SIZE_COUNT) {
                    Piece.Size missing = findMissingSize(sizes);
                    Position source = missing == null ? null : findMovablePieceOfSize(color, missing);

                    if (source != null && isLegalMove(source, position, color)) {
                        position.placePiece(source.removeTopPiece());
                        model.checkAndBlockTree(row, col);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Blocks a nearby opponent tree threat when possible
    private boolean blockOpponent(Piece.Color botColor) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece.Color threat = detectThreat(row, col);
                if (threat == null || controlledColors.contains(threat)) {
                    continue;
                }

                Position target = model.getCell(row, col);
                Position blocker = findBlocker(botColor, target);
                if (blocker != null) {
                    target.placePiece(blocker.removeTopPiece());
                    model.checkAndBlockTree(row, col);
                    return true;
                }
            }
        }

        return false;
    }

    // Finds a bot piece that can block the target position
    private Position findBlocker(Piece.Color botColor, Position target) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Position from = model.getCell(row, col);
                if (from.getStack().isEmpty()) {
                    continue;
                }

                Piece piece = from.getStack().peek();
                if (piece.getColor() == botColor && piece.getSize() != Piece.Size.LARGE && isLegalMove(from, target, botColor)) {
                    return from;
                }
            }
        }

        return null;
    }

    // Chooses the highest scoring simple move
    private boolean smartMove(Piece.Color color) {
        int bestScore = Integer.MIN_VALUE;
        Position bestFrom = null;
        Position bestTo = null;

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Position from = model.getCell(row, col);
                if (from.getStack().isEmpty()) {
                    continue;
                }

                Piece top = from.getStack().peek();
                if (top.getColor() != color || top.getSize() == Piece.Size.LARGE) {
                    continue;
                }

                MoveChoice choice = findBestNeighborMove(from, top, color, bestScore);
                if (choice != null) {
                    bestScore = choice.score();
                    bestFrom = choice.from();
                    bestTo = choice.to();
                }
            }
        }

        if (bestFrom != null && bestTo != null) {
            bestTo.placePiece(bestFrom.removeTopPiece());
            model.checkAndBlockTree(bestTo.getRow(), bestTo.getCol());
            return true;
        }

        return false;
    }

    // Finds the best one-step move around a piece
    private MoveChoice findBestNeighborMove(Position from, Piece movingPiece, Piece.Color color, int currentBestScore) {
        MoveChoice bestChoice = null;

        for (int[] direction : DIRECTIONS) {
            int nextRow = from.getRow() + direction[0];
            int nextCol = from.getCol() + direction[1];

            if (!model.isInside(nextRow, nextCol)) {
                continue;
            }

            Position to = model.getCell(nextRow, nextCol);
            if (!isLegalMove(from, to, color) || countNeighbors(to, color) == 0) {
                continue;
            }

            int potential = evaluateTreePotential(to, color, movingPiece.getSize());
            if (potential == NO_TREE_POTENTIAL) {
                continue;
            }

            int score = evaluatePosition(to, color) + potential;
            if (score > currentBestScore) {
                bestChoice = new MoveChoice(from, to, score);
                currentBestScore = score;
            }
        }

        return bestChoice;
    }

    // Checks whether a bot piece can move to a target position
    private boolean isLegalMove(Position from, Position to, Piece.Color color) {
        if (from == null || to == null) {
            return false;
        }

        if (from.getStack().isEmpty() || to.getStack().size() >= MAX_STACK_SIZE_FOR_MOVE) {
            return false;
        }

        Piece moving = from.getStack().peek();
        if (moving.getColor() != color) {
            return false;
        }

        return to.getStack().isEmpty() || to.getStack().peek().getColor() == color;
    }

    // Checks if one more size can complete a tree
    private boolean isOneMoveFromTree(int row, int col, Piece.Color color) {
        return getMatchingSizes(model.getCell(row, col), color).size() == NEAR_TREE_SIZE_COUNT;
    }

    // Counts same-color pieces around a position
    private int countNeighbors(Position position, Piece.Color color) {
        int count = 0;

        for (int[] direction : DIRECTIONS) {
            int row = position.getRow() + direction[0];
            int col = position.getCol() + direction[1];

            if (model.isInside(row, col)) {
                Position neighbor = model.getCell(row, col);
                if (!neighbor.getStack().isEmpty() && neighbor.getStack().peek().getColor() == color) {
                    count++;
                }
            }
        }

        return count;
    }

    // Scores how useful a move is for building a tree
    private int evaluateTreePotential(Position position, Piece.Color color, Piece.Size movingSize) {
        List<Piece.Size> sizes = getMatchingSizes(position, color);
        if (sizes.contains(movingSize)) {
            return NO_TREE_POTENTIAL;
        }

        sizes.add(movingSize);
        return sizes.size() == COMPLETE_TREE_SIZE_COUNT ? COMPLETE_TREE_BONUS : PARTIAL_TREE_BONUS;
    }

    // Scores a position using nearby friendly pieces
    private int evaluatePosition(Position position, Piece.Color color) {
        return countNeighbors(position, color) * NEIGHBOR_SCORE_WEIGHT;
    }

    // Detects whether a position has a two-piece threat
    private Piece.Color detectThreat(int row, int col) {
        Position position = model.getCell(row, col);
        Map<Piece.Color, Set<Piece.Size>> colorSizes = new HashMap<>();

        for (Piece piece : position.getStack()) {
            colorSizes.computeIfAbsent(piece.getColor(), key -> new HashSet<>()).add(piece.getSize());
        }

        for (Map.Entry<Piece.Color, Set<Piece.Size>> entry : colorSizes.entrySet()) {
            if (entry.getValue().size() == NEAR_TREE_SIZE_COUNT) {
                return entry.getKey();
            }
        }

        return null;
    }

    // Finds the missing size in a partial tree
    private Piece.Size findMissingSize(List<Piece.Size> sizes) {
        return Arrays.stream(Piece.Size.values())
                .filter(size -> !sizes.contains(size))
                .findFirst()
                .orElse(null);
    }

    // Finds a movable piece while avoiding a target position
    private Position findMovablePiece(Piece.Color color, Piece.Size size, int targetRow, int targetCol) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (row == targetRow && col == targetCol) {
                    continue;
                }

                Position position = model.getCell(row, col);
                if (hasMatchingTopPiece(position, color, size)) {
                    return position;
                }
            }
        }

        return null;
    }

    // Finds any movable piece of the requested size
    private Position findMovablePieceOfSize(Piece.Color color, Piece.Size size) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Position position = model.getCell(row, col);
                if (hasMatchingTopPiece(position, color, size)) {
                    return position;
                }
            }
        }

        return null;
    }

    // Checks whether the top piece matches the requested color and size
    private boolean hasMatchingTopPiece(Position position, Piece.Color color, Piece.Size size) {
        return !position.getStack().isEmpty()
                && position.getStack().peek().getColor() == color
                && position.getStack().peek().getSize() == size;
    }

    // Returns all piece sizes on a position that match one color
    private List<Piece.Size> getMatchingSizes(Position position, Piece.Color color) {
        List<Piece.Size> sizes = new ArrayList<>();

        for (Piece piece : position.getStack()) {
            if (piece.getColor() == color) {
                sizes.add(piece.getSize());
            }
        }

        return sizes;
    }

    // Checks whether a color is warm
    private boolean isWarmColor(Piece.Color color) {
        return color == Piece.Color.RED || color == Piece.Color.YELLOW;
    }

    // Decides whether to try a tree-focused action this turn
    private boolean shouldTryTreeAction() {
        return random.nextDouble() <= TREE_ACTION_CHANCE;
    }

    // Stores one possible bot move and its score
    private record MoveChoice(Position from, Position to, int score) {
    }
}