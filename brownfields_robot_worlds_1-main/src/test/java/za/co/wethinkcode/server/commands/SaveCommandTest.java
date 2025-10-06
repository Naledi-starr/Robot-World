package za.co.wethinkcode.server.commands;

import org.junit.jupiter.api.*;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class SaveCommandTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        // In-memory SQLite DB
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Simple schema (adjust column names/types to match schema.sql)
            stmt.execute("""
                CREATE TABLE obstacles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    obstacle_name TEXT NOT NULL UNIQUE
                );
            """);
            stmt.execute("""
                CREATE TABLE saved_worlds (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_name TEXT NOT NULL UNIQUE,
                    world_size INTEGER NOT NULL
                );
            """);
            stmt.execute("""
                CREATE TABLE world_properties (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_id INTEGER NOT NULL,
                    obstacle_id INTEGER NOT NULL,
                    x_cord INTEGER NOT NULL,
                    y_cord INTEGER NOT NULL,
                    obstacle_width INTEGER NOT NULL,
                    obstacle_height INTEGER NOT NULL,
                    FOREIGN KEY (world_id) REFERENCES saved_worlds(id) ON DELETE CASCADE,
                    FOREIGN KEY (obstacle_id) REFERENCES obstacles(id) ON DELETE CASCADE
                );
            """);

            // Seed obstacles
            stmt.execute("INSERT INTO obstacles (obstacle_name) VALUES ('lake'),('mountain');");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Helper to create a test world
    private World createTestWorld(int width, int height) {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH",  String.valueOf(width));
            properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
            properties.setProperty("NUM_PITS",      "0");
            properties.setProperty("NUM_MOUNTAINS","0");
            properties.setProperty("NUM_LAKES",     "0");
            properties.setProperty("VISIBILITY_RANGE","5");
            properties.setProperty("MAX_SHIELD_STRENGTH","5");
            properties.setProperty("REPAIR_TIME","5");
            properties.setProperty("RELOAD_TIME","5");
        }};
        return new World(config);
    }

    @Test
    void testWorldNameExists() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO saved_worlds (world_name, world_size) VALUES (?, ?)")) {
            ps.setString(1, "world1");
            ps.setInt(2, 100);
            ps.executeUpdate();
        }

        SaveCommand command = new SaveCommand(createTestWorld(100, 100));

        assertTrue(command.worldNameExists(connection, "world1"));
        assertFalse(command.worldNameExists(connection, "unknownWorld"));
    }

    @Test
    void testGenerateNextDefaultName() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO saved_worlds (world_name, world_size) VALUES (?, ?)")) {
            ps.setString(1, "world1");
            ps.setInt(2, 100);
            ps.executeUpdate();

            ps.setString(1, "world2");
            ps.setInt(2, 200);
            ps.executeUpdate();
        }

        SaveCommand command = new SaveCommand(createTestWorld(100, 100));
        String nextName = command.generateNextDefaultName(connection);
        assertEquals("world3", nextName);
    }

    @Test
    void testGetObstacleId() throws Exception {
        SaveCommand command = new SaveCommand(createTestWorld(100, 100));
        int id = command.getObstacleId(connection, "lake");
        assertTrue(id > 0);

        SQLException ex = assertThrows(SQLException.class, () -> command.getObstacleId(connection, "unknown"));
        assertTrue(ex.getMessage().contains("Obstacle type not found"));
    }
}
