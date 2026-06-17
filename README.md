# Blockade

**Author:** Tanmoy Das  
**Course/Year:** 1st year (ACS - KdG)

**Project type:** Java + JavaFX desktop board game using an MVP architecture

Blockade is a first-year JavaFX board game project inspired by Kristin Looney's Icehouse-style board games. The application lets a player register or log in, choose either the warm or cold colour team, play against a bot, save game progress to a PostgreSQL database, and view game statistics and leaderboard data.

## Technologies Used

- **Java** - main programming language for the game logic, models, presenters, and application entry point.
- **JavaFX** - GUI framework used for windows, buttons, board cells, dice display, score display, alerts, and navigation screens.
- **SQL / PostgreSQL** - database used for players, games, pieces, moves, scores, and leaderboard data.
- **Properties configuration** - `config/database.properties` stores local database connection settings.
- **MVP architecture** - the project is separated into Model, View, and Presenter layers.

This is not a Maven or Gradle project. It is set up like a normal IntelliJ IDEA JavaFX project with libraries added manually.

## Main Features

- Player registration and login.
- Start menu with game rules and leaderboard access.
- Warm colour mode: player controls **red** and **yellow**.
- Cold colour mode: player controls **blue** and **green**.
- Human player versus bot.
- Dice-based movement points for each controlled colour.
- 5x5 game board with stacked pieces.
- Small, medium, large, clear, and black blockade pieces.
- Automatic scoring when a tree is completed.
- Manual end-game button.
- Game statistics screen after a game ends.
- PostgreSQL-backed storage for users, games, pieces, moves, scores, and history.

## How The Game Works

The board is a **5 by 5 grid**. Pieces are stacked on board cells. The four playable colours are split into two teams:

- **Warm team:** red and yellow
- **Cold team:** blue and green

The human player chooses either the warm team or the cold team before the game starts. The bot automatically controls the opposite team.

Each colour has pieces with three sizes:

- **Large** - base piece for a tree.
- **Medium** - middle piece for a tree.
- **Small** - top piece for a tree.

A completed tree is made when pieces of the same colour are stacked in this order:

```text
Small
Medium
Large
```

When a valid tree is completed, the game places a **black blockade piece** on top of it. That tree then counts as one point for its team.

## Starting Board

The board begins with a fixed pattern of red, yellow, blue, green, and clear cells. Most coloured cells start with a large piece and a small piece. The medium pieces begin in the corner supply stacks:

- Green medium pieces start in the top-left corner.
- Red medium pieces start in the top-right corner.
- Yellow medium pieces start in the bottom-left corner.
- Blue medium pieces start in the bottom-right corner.

The goal is to move pieces around the board to complete trees for your team.

## Turn Rules

1. Click **Roll Dice** at the start of your turn.
2. The game rolls one die for each colour you control.
   - Warm player: red die and yellow die.
   - Cold player: blue die and green die.
3. The dice values become movement points for those colours.
4. Click a piece to select it.
5. Valid destination cells are highlighted in green.
6. Click a destination to move the selected piece.
7. The movement cost is based on the number of board spaces moved.
8. A piece can only use movement points from its own colour die.
9. When your movement points are used, click **End Turn**.
10. The bot then takes its turn automatically.

## Movement Rules

Pieces move in straight lines:

- Horizontally
- Vertically
- Diagonally

A move is invalid if:

- The destination is the same as the starting cell.
- The move is not horizontal, vertical, or diagonal.
- The piece does not have enough movement points.
- The piece tries to land on an opponent's piece.
- The piece tries to make an illegal stack.
- The path is blocked by an opponent's completed black blockade tree.

Pieces can land on:

- Empty cells.
- Clear cells.
- A legal stack of the same colour.

The intended tree-building order is large at the bottom, medium in the middle, and small on top. Large pieces are treated as base pieces in the game rules.

## Scoring And Winning

Each completed tree is worth **1 point** for its colour team.

- Red and yellow trees increase the warm score.
- Blue and green trees increase the cold score.

The game checks whether a colour has completed all five of its trees. If either colour in a team completes all its trees, that team wins:

- Red or yellow completes all trees: **Warm player wins**.
- Blue or green completes all trees: **Cold player wins**.

If the game is ended manually, the current warm and cold scores are saved and the game statistics screen opens.

## Bot Behaviour

The bot controls the team not chosen by the human player. It rolls dice for its colours and then tries to make useful moves. Its move logic can:

- Try to complete a tree.
- Prepare a future tree.
- Move towards friendly pieces.
- Block an opponent's possible tree.

## Application Flow

```text
Login/Register
      |
      v
Main Setup Screen
      |
      v
Choose Warm Colours or Cold Colours
      |
      v
Game Board
      |
      v
Game Stats / Leaderboard
```

## Project Structure

```text
src/main/java/blockaders/
|-- Main.java
|-- BlockadeApplication.java
|-- model/
|   |-- board/
|   |-- database/
|   |-- game/
|   |-- leaderboard/
|   `-- player/
|-- presenter/
|   |-- board/
|   |-- leaderboard/
|   |-- login/
|   |-- rules/
|   |-- setup/
|   `-- stats/
`-- view/
    |-- board/
    |-- leaderboard/
    |-- login/
    |-- rules/
    |-- setup/
    `-- stats/
```

## MVP Architecture

The project follows **MVP: Model View Presenter**.

- **Model** contains the data and rules, such as the board, pieces, dice, players, bot, game state, leaderboard model, and database access.
- **View** contains JavaFX screens and controls. Views display the board, login screen, setup screens, rules, leaderboard, and game stats.
- **Presenter** connects the views to the models. Presenters handle button clicks, board clicks, movement, screen navigation, saving moves, and ending games.

Important examples:

- `LoginView` + `LoginPresenter`
- `SetUpView` + `SetUpPresenter`
- `ColorsSetUpView` + `ColorsSetUpPresenter`
- `BoardView` + `BoardPresenter`
- `BoardColdView` + `BoardColdPresenter`
- `LeaderboardView` + `LeaderboardPresenter`
- `GameStatsView` + `GameStatsPresenter`

## Database Setup

The project uses PostgreSQL. The database schema is in:

```text
database/schema.sql
```

Tables used by the project:

- `players`
- `games`
- `pieces`
- `moves`

To configure the database:

1. Create a PostgreSQL database.
2. Run `database/schema.sql` in that database.
3. Copy `config/database.example.properties` to `config/database.properties`.
4. Update the values:

```properties
db.url=jdbc:postgresql://localhost:5432/game
db.user=game
db.password=your-password
```

The app can also read these environment variables:

```text
BLOCKADE_DB_URL
BLOCKADE_DB_USER
BLOCKADE_DB_PASSWORD
```

## Requirements

- Java 21 or newer
- JavaFX SDK 21 or newer
- PostgreSQL
- PostgreSQL JDBC driver
- IntelliJ IDEA

The included IntelliJ module file currently points to:

```text
/Users/tanmoydas/Desktop/javafx-sdk-21.0.8
/Users/tanmoydas/Desktop/postgresql-42.7.7.jar
```

If your files are stored somewhere else, update the JavaFX and PostgreSQL library paths in IntelliJ.

## How To Run

1. Open the project folder in IntelliJ IDEA.
2. Set the Project SDK to Java 21 or newer.
3. Add the JavaFX SDK `lib` folder as a project library.
4. Add the PostgreSQL JDBC `.jar` as a project library.
5. Make sure the `resources` folder is marked as a resources root.
6. Set up the PostgreSQL database and `config/database.properties`.
7. Run:

```text
blockaders.Main
```

`blockaders.Main` is the correct entry point because it launches the JavaFX application class.

## Manual Testing Checklist

- Register a new player.
- Log in with that player.
- Open the game rules screen.
- Open the leaderboard screen.
- Start a new game.
- Choose warm colours.
- Roll dice.
- Move red and yellow pieces.
- End the turn and let the bot play.
- End the game and check game stats.
- Start another game and choose cold colours.
- Roll dice.
- Move blue and green pieces.
- Confirm that scores update when trees are completed.
- Confirm that moves and final score are saved to PostgreSQL.