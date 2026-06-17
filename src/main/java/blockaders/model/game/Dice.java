package blockaders.model.game;

import java.util.Random;

public class Dice {
    // Lowest value a die can roll
    private static final int MIN_ROLL = 1;

    // Number of sides on one die
    private static final int SIDES = 6;

    // Value before the first roll
    private static final int NOT_ROLLED = 0;

    private final Random random = new Random();
    private final String color;
    private int value;

    // Creates a die for one piece color
    public Dice(String color) {
        this.color = color;
        this.value = NOT_ROLLED;
    }

    // Returns the color assigned to this die
    public String getColor() {
        return color;
    }

    // Rolls the die and returns the new value
    public int roll() {
        value = random.nextInt(SIDES) + MIN_ROLL;
        return value;
    }
}