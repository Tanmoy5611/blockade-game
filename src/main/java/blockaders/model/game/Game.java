package blockaders.model.game;

import blockaders.model.player.Player;

import java.util.List;

public class Game {
    // Number of human players supported by this version
    private static final int REQUIRED_HUMAN_PLAYERS = 1;

    // Default score used before scoring starts
    private static final int DEFAULT_SCORE = 0;

    private int coldColorScore = DEFAULT_SCORE;
    private int warmColorScore = DEFAULT_SCORE;

    // Creates a game score holder for one human player against the bot
    public Game(List<Player> players) {
        if (players == null || players.size() != REQUIRED_HUMAN_PLAYERS) {
            throw new IllegalArgumentException("Exactly one human player is required to play against the bot");
        }
    }

    // Returns winner text based on warm and cold scores
    public String getWinnerText() {
        if (coldColorScore > warmColorScore) {
            return "Cold player has won the game!";
        }

        if (warmColorScore > coldColorScore) {
            return "Warm player has won the game!";
        }

        return "The game ended in a draw!";
    }

    // Updates the cold score
    public void setColdScore(int coldScore) {
        this.coldColorScore = coldScore;
    }

    // Updates the warm score
    public void setWarmScore(int warmScore) {
        this.warmColorScore = warmScore;
    }

}