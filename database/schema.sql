CREATE TABLE IF NOT EXISTS players (
    player_id SERIAL PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS games (
    game_id SERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    player_id INTEGER NOT NULL REFERENCES players(player_id),
    color VARCHAR(20) NOT NULL,
    score INTEGER DEFAULT 0,
    duration INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pieces (
    piece_id SERIAL PRIMARY KEY,
    color VARCHAR(20) NOT NULL,
    size VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS moves (
    move_id SERIAL PRIMARY KEY,
    game_id INTEGER NOT NULL REFERENCES games(game_id),
    move_timestamp INTEGER NOT NULL,
    from_row INTEGER NOT NULL,
    from_col INTEGER NOT NULL,
    to_row INTEGER NOT NULL,
    to_col INTEGER NOT NULL,
    piece_id INTEGER REFERENCES pieces(piece_id)
);