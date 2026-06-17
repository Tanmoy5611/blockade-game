package blockaders.model.player;

import blockaders.model.board.Piece;

public abstract class Player {
    private final int id;
    private final String name;
    private final int score;
    private final Piece.Color color;

    // Creates a player with profile and game data
    public Player(int id, String name, int score, Piece.Color color) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.color = color;
    }

    // Returns the player name
    public String getName() {
        return name;
    }

    // Returns the player color
    public Piece.Color getColor() {
        return color;
    }

    // Returns the current score
    public int getScore() {
        return score;
    }

    // Returns the player id
    public int getId() {
        return id;
    }
}