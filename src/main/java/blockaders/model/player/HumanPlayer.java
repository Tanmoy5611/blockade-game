package blockaders.model.player;

import blockaders.model.board.Piece;

public class HumanPlayer extends Player {
    // Default color used before the player chooses a side
    private static final Piece.Color DEFAULT_COLOR = Piece.Color.RED;

    // Creates a human player with an explicit color
    public HumanPlayer(int playerId, String name, int score, Piece.Color color) {
        super(playerId, name, score, color);
    }

    // Creates a human player with the default color
    public HumanPlayer(int playerId, String name, int score) {
        super(playerId, name, score, DEFAULT_COLOR);
    }
}