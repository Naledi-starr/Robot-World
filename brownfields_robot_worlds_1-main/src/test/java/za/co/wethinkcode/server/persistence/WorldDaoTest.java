package za.co.wethinkcode.server.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Lake;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class WorldDaoTest {
    private WorldDao worldDao;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Create a single in-memory database connection
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        worldDao = new WorldDao(connection);
        setupTestDatabase();
    }

    private void setupTestDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create tables
            stmt.execute("CREATE TABLE world_properties (property_name TEXT PRIMARY KEY, value INTEGER)");
            stmt.execute("CREATE TABLE obstacles (type TEXT, x INTEGER, y INTEGER, width INTEGER, height INTEGER)");
            // Insert test data
            stmt.execute("INSERT INTO world_properties (property_name, value) VALUES ('WORLD_WIDTH', 200)");
            stmt.execute("INSERT INTO world_properties (property_name, value) VALUES ('WORLD_HEIGHT', 150)");
            stmt.execute("INSERT INTO obstacles (type, x, y, width, height) VALUES ('mountain', 10, 10, 5, 5)");
            stmt.execute("INSERT INTO obstacles (type, x, y, width, height) VALUES ('lake', 20, 20, 3, 3)");

            // Debug: Verify table creation and data
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM world_properties")) {
                System.out.println("WorldDaoTest: world_properties contents:");
                while (rs.next()) {
                    System.out.println(rs.getString("property_name") + ": " + rs.getInt("value"));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM obstacles")) {
                System.out.println("WorldDaoTest: obstacles contents:");
                while (rs.next()) {
                    System.out.println(rs.getString("type") + " at (" + rs.getInt("x") + "," + rs.getInt("y") + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to set up test database: " + e.getMessage());
            throw e;
        }
    }

//    @Test
//    void testRestoreWorld() throws SQLException {
//        World world = worldDao.restoreWorld();
//
//        // Debug: Verify world properties
//        System.out.println("WorldDaoTest: Restored world width: " + world.getWidth());
//        System.out.println("WorldDaoTest: Restored world height: " + world.getHeight());
//        System.out.println("WorldDaoTest: Restored obstacles count: " + world.getObstacles().size());
//
//        // Verify world dimensions
//        assertEquals(200, world.getWidth(), "World width should be 200");
//        assertEquals(150, world.getHeight(), "World height should be 150");
//
//        // Verify obstacles
//        List<?> obstacles = world.getObstacles();
//        assertEquals(2, obstacles.size(), "Should have 2 obstacles");
//        assertTrue(obstacles.stream().anyMatch(o -> o instanceof Mountain &&
//                        getObstacleX(o) == 10 && getObstacleY(o) == 10 &&
//                        getObstacleWidth(o) == 5 && getObstacleHeight(o) == 5),
//                "Should contain a mountain at (10, 10) with size 5x5");
//        assertTrue(obstacles.stream().anyMatch(o -> o instanceof Lake &&
//                        getObstacleX(o) == 20 && getObstacleY(o) == 20 &&
//                        getObstacleWidth(o) == 3 && getObstacleHeight(o) == 3),
//                "Should contain a lake at (20, 20) with size 3x3");
//    }

    @Test
    void testRestoreWorldMissingProperty() throws SQLException {
        // Drop world_properties to simulate missing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE world_properties");
        } catch (SQLException e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        SQLException thrown = assertThrows(SQLException.class, () -> worldDao.restoreWorld(),
                "Expected SQLException for missing table or property");
        assertTrue(thrown.getMessage().contains("no such table") ||
                        thrown.getMessage().contains("World dimensions not found"),
                "Error message should indicate missing table or property");
    }

    // Helper methods for obstacle properties
    private int getObstacleX(Object obstacle) {
        try {
            return (Integer) obstacle.getClass().getMethod("getX").invoke(obstacle);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access obstacle x", e);
        }
    }

    private int getObstacleY(Object obstacle) {
        try {
            return (Integer) obstacle.getClass().getMethod("getY").invoke(obstacle);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access obstacle y", e);
        }
    }

    private int getObstacleWidth(Object obstacle) {
        try {
            return (Integer) obstacle.getClass().getMethod("getWidth").invoke(obstacle);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access obstacle width", e);
        }
    }

    private int getObstacleHeight(Object obstacle) {
        try {
            return (Integer) obstacle.getClass().getMethod("getHeight").invoke(obstacle);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access obstacle height", e);
        }
    }
}