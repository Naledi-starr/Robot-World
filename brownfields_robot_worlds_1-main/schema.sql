PRAGMA foreign_keys = on;

CREATE TABLE IF NOT EXISTS obstacles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    obstacle_name NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS saved_worlds (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    world_name NOT NULL UNIQUE,
    world_size INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS world_properties (
    world_id INTEGER NOT NULL,
    obstacle_id INTEGER NOT NULL,
    x_cord INTEGER NOT NULL,
    y_cord INTEGER NOT NULL,
    obstacle_width INTEGER NOT NULL,
    obstacle_height INTEGER NOT NULL,
    FOREIGN KEY (world_id) REFERENCES saved_worlds(id) ON DELETE CASCADE,
    FOREIGN KEY (obstacle_id) REFERENCES obstacles(id) ON DELETE CASCADE
);

-- INSERT default obstacles
INSERT OR IGNORE INTO obstacles (obstacle_name) VALUES
    ('lake'),
    ('bottomlesspit'),
    ('mountain'),
    ('mine');