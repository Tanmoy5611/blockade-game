package blockaders.model.game;

public class GameStats {
    // Default winner text before stats are loaded
    private static final String DEFAULT_WINNER = "Unknown";

    // Default whole-number stat value
    private static final int DEFAULT_INT_VALUE = 0;

    // Default decimal stat value
    private static final double DEFAULT_DOUBLE_VALUE = 0.0;

    public String winner = DEFAULT_WINNER;
    public int totalPlayTime = DEFAULT_INT_VALUE;
    public int totalMoves = DEFAULT_INT_VALUE;
    public double avgMoveDuration = DEFAULT_DOUBLE_VALUE;
    public int totalScore = DEFAULT_INT_VALUE;
}