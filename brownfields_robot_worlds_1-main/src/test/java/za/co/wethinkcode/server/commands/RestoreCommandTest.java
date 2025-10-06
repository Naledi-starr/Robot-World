package za.co.wethinkcode.server.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.persistence.WorldDao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RestoreCommand class, focusing on edge cases for world restoration.
 */
public class RestoreCommandTest {
    private Connection connection;
    private WorldDao worldDao;
    private RestoreCommand restoreCommand;

    /**
     * Sets up an in-memory SQLite database and initializes the RestoreCommand before each test.
     *
     * @throws SQLException if database setup fails
     */
    @BeforeEach
    void setUp() throws SQLException {
        // Use in-memory database for isolated testing
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        worldDao = new WorldDao(connection);
        restoreCommand = new RestoreCommand(worldDao);
        setupBaseDatabase();
    }

    /**
     * Initializes the database schema with obstacles table.
     *
     * @throws SQLException if schema creation fails
     */
    private void setupBaseDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = on");
            stmt.execute("CREATE TABLE obstacles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "obstacle_name TEXT NOT NULL UNIQUE)");
            stmt.execute("INSERT INTO obstacles (obstacle_name) VALUES ('mountain'), ('lake'), ('bottomlessPit'), ('mine')");
        }
    }

    /**
     * Tests restoring an empty world with no obstacles or robots.
     */
//    @Test
//    void testRestoreEmptyWorld() throws SQLException {
//        // Arrange: Create a world with size 50x50, no obstacles
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute("CREATE TABLE world_config (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "world_name TEXT NOT NULL UNIQUE, " +
//                    "world_size INTEGER NOT NULL, " +
//                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
//            stmt.execute("INSERT INTO world_config (world_name, world_size) VALUES ('empty_world', 50)");
//        }
//
//        // Act: Execute restore command
//        String result = restoreCommand.execute();
//
//        // Assert: Verify world is restored with correct size and no obstacles
//        World restoredWorld = RobotWorldServer.getWorld();
//        assertEquals("✅ World restored successfully", result, "Should return success message");
//        assertEquals(50, restoredWorld.getWidth(), "World width should be 50");
//        assertEquals(50, restoredWorld.getHeight(), "World height should be 50");
//        assertTrue(restoredWorld.getObstacles().isEmpty(), "World should have no obstacles");
//    }
//
//    /**
//     * Tests restoring a world with valid size but no obstacles.
//     */
//    @Test
//    void testRestoreWorldWithNoObstacles() throws SQLException {
//        // Arrange: Create a world with size 100x100, no obstacles
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute("CREATE TABLE world_config (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "world_name TEXT NOT NULL UNIQUE, " +
//                    "world_size INTEGER NOT NULL, " +
//                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
//            stmt.execute("INSERT INTO world_config (world_name, world_size) VALUES ('no_obstacles_world', 100)");
//        }
//
//        // Act: Execute restore command
//        String result = restoreCommand.execute();
//
//        // Assert: Verify world is restored with correct size and no obstacles
//        World restoredWorld = RobotWorldServer.getWorld();
//        assertEquals("✅ World restored successfully", result, "Should return success message");
//        assertEquals(100, restoredWorld.getWidth(), "World width should be 100");
//        assertEquals(100, restoredWorld.getHeight(), "World height should be 100");
//        assertTrue(restoredWorld.getObstacles().isEmpty(), "World should have no obstacles");
//    }
//
//    /**
//     * Tests restoring a world with multiple obstacles (mountains, lakes, mines).
//     */
//    @Test
//    void testRestoreWorldWithMultipleObstacles() throws SQLException {
//        // Arrange: Create a world with size 200x200 and multiple obstacles
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute("CREATE TABLE world_config (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "world_name TEXT NOT NULL UNIQUE, " +
//                    "world_size INTEGER NOT NULL, " +
//                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
//            stmt.execute("CREATE TABLE world_setup (" +
//                    "world_id INTEGER NOT NULL, " +
//                    "obstacle_id INTEGER NOT NULL, " +
//                    "x_cord INTEGER NOT NULL, " +
//                    "y_cord INTEGER NOT NULL, " +
//                    "FOREIGN KEY (world_id) REFERENCES world_config(id) ON DELETE CASCADE, " +
//                    "FOREIGN KEY (obstacle_id) REFERENCES obstacles(id) ON DELETE CASCADE)");
//            stmt.execute("INSERT INTO world_config (world_name, world_size) VALUES ('full_world', 200)");
//            stmt.execute("INSERT INTO world_setup (world_id, obstacle_id, x_cord, y_cord) " +
//                    "SELECT 1, id, 10, 10 FROM obstacles WHERE obstacle_name = 'mountain'");
//            stmt.execute("INSERT INTO world_setup (world_id, obstacle_id, x_cord, y_cord) " +
//                    "SELECT 1, id, 20, 20 FROM obstacles WHERE obstacle_name = 'lake'");
//            stmt.execute("INSERT INTO world_setup (world_id, obstacle_id, x_cord, y_cord) " +
//                    "SELECT 1, id, 30, 30 FROM obstacles WHERE obstacle_name = 'mine'");
//            stmt.execute("INSERT INTO world_setup (world_id, obstacle_id, x_cord, y_cord) " +
//                    "SELECT 1, id, 40, 40 FROM obstacles WHERE obstacle_name = 'mountain'");
//        }
//
//        // Act: Execute restore command
//        String result = restoreCommand.execute();
//
//        // Assert: Verify world is restored with correct size and obstacles
//        World restoredWorld = RobotWorldServer.getWorld();
//        assertEquals("✅ World restored successfully", result, "Should return success message");
//        assertEquals(200, restoredWorld.getWidth(), "World width should be 200");
//        assertEquals(200, restoredWorld.getHeight(), "World height should be 200");
//        List<Obstacle> obstacles = restoredWorld.getObstacles();
//        assertEquals(4, obstacles.size(), "Should have 4 obstacles");
//        assertTrue(obstacles.stream().anyMatch(o -> o instanceof Mountain &&
//                o.getX() == 10 && o.getY() == 10 &&
//                o.getWidth() == 1 && o.getHeight() == 1 &&
//                o.getType().equals("Mountain")), "Should contain mountain at (10,10)");
//        assertTrue(obstacles.stream().anyMatch(o -> o instanceof Lake &&
//                o.getX() == 20 && o.getY() == 20 &&
//                o.getWidth() == 1 && o.getHeight() == 1 &&
//                o.getType().equals("Lake")), "Should contain lake at (20,20)");
//        assertTrue(obstacles.stream().anyMatch(o -> o instanceof Mine &&
//                o.getX() == 30 && o.getY() == 30 &&
//                o.getWidth() == 1 && o.getHeight() == 1 &&
//                o.getType().equals("Mine")), "Should contain mine at (30,30)");
//        assertTrue(obstacles.stream().anyMatch(o -> o instanceof Mountain &&
//                o.getX() == 40 && o.getY() == 40 &&
//                o.getWidth() == 1 && o.getHeight() == 1 &&
//                o.getType().equals("Mountain")), "Should contain mountain at (40,40)");
//    }

    /**
     * Tests restoring a world when the world_config table is missing.
     */
    @Test
    void testRestoreWithMissingWorldConfigTable() {
        // Arrange: Do not create world_config table to simulate missing table
        // (obstacles table is already created in setupBaseDatabase)

        // Act & Assert: Verify SQLException is thrown
        SQLException thrown = assertThrows(SQLException.class, () -> restoreCommand.execute(),
                "Expected SQLException for missing world_config table");
        assertTrue(thrown.getMessage().contains("no such table") ||
                        thrown.getMessage().contains("No world found"),
                "Error message should indicate missing table or no world");
    }
}