package blockaders.model.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PlayerStats {
    private final StringProperty statistic;

    public PlayerStats(String statistic) {
        this.statistic = new SimpleStringProperty(statistic);
    }

    public StringProperty statisticProperty() {
        return statistic;
    }

}