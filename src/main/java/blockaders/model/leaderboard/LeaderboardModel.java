package blockaders.model.leaderboard;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LeaderboardModel {
    private final StringProperty playerName;
    private final IntegerProperty totalScore;
    private final IntegerProperty gamesPlayed;
    private final IntegerProperty gamesWon;
    private final IntegerProperty gamesLost;
    private final IntegerProperty gamesDrawn;

    // Creates one row for the leaderboard table
    public LeaderboardModel(String playerName, int totalScore, int gamesPlayed, int gamesWon, int gamesLost, int gamesDrawn) {
        this.playerName = new SimpleStringProperty(playerName);
        this.totalScore = new SimpleIntegerProperty(totalScore);
        this.gamesPlayed = new SimpleIntegerProperty(gamesPlayed);
        this.gamesWon = new SimpleIntegerProperty(gamesWon);
        this.gamesLost = new SimpleIntegerProperty(gamesLost);
        this.gamesDrawn = new SimpleIntegerProperty(gamesDrawn);
    }

    // Returns the player name property for JavaFX tables
    public StringProperty playerNameProperty() {
        return playerName;
    }

    // Returns the total score property for JavaFX tables
    public IntegerProperty totalScoreProperty() {
        return totalScore;
    }

    // Returns the games played property for JavaFX tables
    public IntegerProperty gamesPlayedProperty() {
        return gamesPlayed;
    }

    // Returns the games won property for JavaFX tables
    public IntegerProperty gamesWonProperty() {
        return gamesWon;
    }

    // Returns the games lost property for JavaFX tables
    public IntegerProperty gamesLostProperty() {
        return gamesLost;
    }

    // Returns the games drawn property for JavaFX tables
    public IntegerProperty gamesDrawnProperty() {
        return gamesDrawn;
    }
}