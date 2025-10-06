package za.co.wethinkcode.database;

import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DbConfigTest {

    private static final String IN_MEMORY_DB_URL = "jdbc:sqlite::memory:";
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(IN_MEMORY_DB_URL);

        // enable foreign key support
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }

        // Apply schema.sql
        String sqlScript = Files.readString(Paths.get("schema.sql"));
        try (Statement stmt = connection.createStatement()) {
            for (String sql : sqlScript.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testConnectionValid() throws SQLException {
        assertNotNull(connection);
        assertTrue(connection.isValid(1));
    }

    @Test
    void testObstaclesInserted() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM obstacles")) {
            assertTrue(rs.next());
            assertEquals(4, rs.getInt("count"), "Default 4 obstacles should exist");
        }
    }

    @Test
    void testInsertWorldAndProperty() throws SQLException {
        // Insert a world
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO saved_worlds (world_name, world_size) VALUES (?, ?)")) {
            ps.setString(1, "TestWorld");
            ps.setInt(2, 200);
            ps.executeUpdate();
        }

        // Insert obstacle reference
        int obstacleId;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM obstacles WHERE obstacle_name = 'lake'")) {
            assertTrue(rs.next());
            obstacleId = rs.getInt("id");
        }

        int worldId;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM saved_worlds WHERE world_name = 'TestWorld'")) {
            assertTrue(rs.next());
            worldId = rs.getInt("id");
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO world_properties (world_id, obstacle_id, x_cord, y_cord, obstacle_width, obstacle_height) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, worldId);
            ps.setInt(2, obstacleId);
            ps.setInt(3, 10);
            ps.setInt(4, 20);
            ps.setInt(5, 5);
            ps.setInt(6, 5);
            ps.executeUpdate();
        }

        // Verify property was inserted
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM world_properties")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("count"), "World property should be inserted");
        }
    }
}
