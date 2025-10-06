package za.co.wethinkcode.server.persistence;

import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;

/**
 * Data Access Object for persisting and restoring the game world to/from an SQLite database.
 */
public class WorldDao {
    private final String dbUrl;

    /**
     * Constructs a WorldDao with the specified database URL.
     *
     * @param dbUrl the SQLite database URL (e.g., "jdbc:sqlite:robot-worlds.db")
     */
    public WorldDao(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * Constructs a WorldDao with a specific database connection for testing.
     *
     * @param connection the SQLite database connection
     */
    public WorldDao(Connection connection) {
        this.dbUrl = null;
        this.connection = connection;
    }

    private Connection connection;

    /**
     * Restores the game world from the database, including its size and obstacles.
     *
     * @return the restored World object
     * @throws SQLException if database access fails or no world is found
     */
    public World restoreWorld() throws SQLException {
        if (dbUrl != null) {
            connection = DriverManager.getConnection(dbUrl);
        }

        // Show all available worlds
        String selectAllWorlds = "SELECT world_name, world_size, created_at FROM saved_worlds";
        try(PreparedStatement pstmt = connection.prepareStatement(selectAllWorlds)) {
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Available Worlds:");
            while (rs.next()) {
                String worldName = rs.getString("world_name");
                int worldSize = rs.getInt("world_size");

                Timestamp ts = rs.getTimestamp("created_at");
                String createdAtStr;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                if (ts != null) {
                    createdAtStr = ts.toLocalDateTime().format(formatter); // convert to LocalDateTime
                } else {
                    createdAtStr = " > 3 weeks ago";
                }

                System.out.printf(" - %s (size: %d, created: %s)%n",
                        worldName, worldSize, createdAtStr);
            }
        }

        World world = null;
        System.out.println("Select world to restore by name in database:");
        Scanner scanner = new Scanner(System.in);
        String worldName = scanner.nextLine();

        try {
            // Try saved_worlds schema (used by RestoreCommandTest)
            try {
                String sql = "SELECT world_size FROM saved_worlds WHERE world_name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)){
                     pstmt.setString(1, worldName);
                     ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("No world found in saved_worlds");
                    }
                    int size = rs.getInt("world_size");
                    world = createDefaultWorld(size);
                    // Query obstacles for saved_worlds schema
                    sql = "SELECT o.obstacle_name, wp.x_cord, wp.y_cord " +
                            "FROM world_properties wp " +
                            "JOIN obstacles o ON wp.obstacle_id = o.id " +
                            "JOIN saved_worlds sw ON wp.world_id = sw.id " +
                            "WHERE sw.world_name = ?";

                    try (PreparedStatement pstmtObstacles = connection.prepareStatement(sql)){
                        pstmtObstacles.setString(1, worldName);
                        ResultSet rsObstacles = pstmtObstacles.executeQuery();
                        while (rsObstacles.next()) {
                            String type = rsObstacles.getString("obstacle_name");
                            int x = rsObstacles.getInt("x_cord");
                            int y = rsObstacles.getInt("y_cord");
                            // Default size 1x1 for saved_worlds schema
                            switch (type.toLowerCase()) {
                                case "mountain":
                                    world.addObstacle(new Mountain(x, y, 1, 1));
                                    break;
                                case "lake":
                                    world.addObstacle(new Lake(x, y, 1, 1));
                                    break;
                                case "pit":
                                    world.addObstacle(new Pit(x, y, 1, 1));
                                    break;
                                case "mine":
                                    world.addObstacle(new Mine(x, y, 1, 1));
                                    break;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                if (!e.getMessage().contains("no such table")) {
                    throw e;
                }
            }
        } finally {
            if (dbUrl != null && connection != null) {
                connection.close();
            }
        }
        return world;
    }

    public World restoreWorldByName(String worldName) throws SQLException {
        if (dbUrl != null) {
            connection = DriverManager.getConnection(dbUrl);
        }
        World world = null;

        try {
            // Try saved_worlds schema (used by RestoreCommandTest)
            try {
                String sql = "SELECT world_size FROM saved_worlds WHERE world_name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)){
                    pstmt.setString(1, worldName);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("No world found in saved_worlds");
                    }
                    int size = rs.getInt("world_size");
                    world = createDefaultWorld(size);

                    // Query obstacles for saved_worlds schema
                    sql = "SELECT o.obstacle_name, wp.x_cord, wp.y_cord " +
                            "FROM world_properties wp " +
                            "JOIN obstacles o ON wp.obstacle_id = o.id " +
                            "JOIN saved_worlds sw ON wp.world_id = sw.id " +
                            "WHERE sw.world_name = ?";

                    try (PreparedStatement pstmtObstacles = connection.prepareStatement(sql)){
                        pstmtObstacles.setString(1, worldName);
                        ResultSet rsObstacles = pstmtObstacles.executeQuery();
                        while (rsObstacles.next()) {
                            String type = rsObstacles.getString("obstacle_name");
                            int x = rsObstacles.getInt("x_cord");
                            int y = rsObstacles.getInt("y_cord");
                            // Default size 1x1 for saved_worldssaved_worlds schema
                            switch (type.toLowerCase()) {
                                case "mountain":
                                    world.addObstacle(new Mountain(x, y, 1, 1));
                                    break;
                                case "lake":
                                    world.addObstacle(new Lake(x, y, 1, 1));
                                    break;
                                case "bottomlesspit":
                                    world.addObstacle(new Pit(x, y, 1, 1));
                                    break;
                                case "mine":
                                    world.addObstacle(new Mine(x, y, 1, 1));
                                    break;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                if (!e.getMessage().contains("no such table")) {
                    throw e;
                }
                // Fall back to world_properties schema (used by WorldDaoTest)
            }
        } finally {
            if (dbUrl != null && connection != null) {
                connection.close();
            }
        }
        return world;
    }

    public Map<String, World> worldsInDatabase(Connection conn) throws SQLException {
        Map<String, World> worldList = new HashMap<>();

        String sql = "SELECT id,world_size,world_name FROM saved_worlds";
        Map<Integer, Obstacle> listObstacles = obstaclesInDatabase(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through all existing world names
            while (rs.next()) {
                int world_id = rs.getInt("id");
                int worldSize = rs.getInt("world_size");
                String worldName = rs.getString("world_name");

                Obstacle obstacle = listObstacles.get(world_id);
                World world = createDefaultWorld(worldSize);
                if (obstacle instanceof Mountain){
                    world.addObstacle((Mountain) obstacle);
                }else if (obstacle instanceof Lake){
                    world.addObstacle((Lake) obstacle);
                }else if (obstacle instanceof Pit){
                    world.addObstacle((Pit) obstacle);
                }else if (obstacle instanceof Mine){
                    world.addObstacle((Mine) obstacle);
                }
                worldList.put(worldName, world);

            }

            return worldList;
        }
    }

    private Map<Integer, Obstacle> obstaclesInDatabase(Connection conn) throws SQLException {
        Map<Integer, Obstacle> obstacles = new HashMap<>();

        String sql = "SELECT world_id,obstacle_id,x_cord,y_cord FROM world_properties";
        Map<Integer, String> listObstacleTypes = obstacleTypesInWorld(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through all existing world names
            while (rs.next()) {
                int world_id = rs.getInt("world_id");
                int obstacleId = rs.getInt("obstacle_id");
                int x_cord = rs.getInt("x_cord");
                int y_cord = rs.getInt("y_cord");

                String obstacleType = listObstacleTypes.get(obstacleId);

                if (obstacleType.equals("Mountain")){
                    Mountain mountain = new Mountain(x_cord, y_cord,1,1);
                    obstacles.put(world_id, mountain);
                }else if (obstacleType.equals("Lake")){
                    Lake lake = new Lake(x_cord, y_cord,1,1);
                    obstacles.put(world_id, lake);
                }else if (obstacleType.equals("Pit")){
                    Pit pit = new Pit(x_cord, y_cord,1,1);
                    obstacles.put(world_id, pit);
                }else if (obstacleType.equals("Mine")){
                    Mine Mine = new Mine(x_cord, y_cord,1,1);
                    obstacles.put(world_id, Mine);
                }
            }
        }

        return obstacles;
    }

    private Map<Integer, String> obstacleTypesInWorld(Connection conn) throws SQLException {
        Map<Integer, String> obstacleTypes = new HashMap<>();

        String sql = "SELECT id,obstacle_name FROM obstacles";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through all existing world names
            while (rs.next()) {
                int obstacleId = rs.getInt("id");
                String obstacleType = rs.getString("obstacle_name");

                obstacleTypes.put(obstacleId, obstacleType);
            }
        }
        return obstacleTypes;
    }

    private World createDefaultWorld(int worldSize){
        Properties props = new Properties();

        props.setProperty("WORLD_WIDTH", String.valueOf(worldSize));
        props.setProperty("WORLD_HEIGHT", String.valueOf(worldSize));
        props.setProperty("NUM_PITS", "0");
        props.setProperty("NUM_MOUNTAINS", "0");
        props.setProperty("NUM_LAKES", "0");
        props.setProperty("VISIBILITY_RANGE", "5");
        props.setProperty("MAX_SHIELD_STRENGTH", "5");
        props.setProperty("REPAIR_TIME", "5");
        props.setProperty("RELOAD_TIME", "5");
        props.setProperty("MAX_SHOTS", "5");
//        System.out.println("WorldDao: Set WORLD_WIDTH=" + worldSize + ", WORLD_HEIGHT=" + worldSize +
//                ", NUM_PITS=0, NUM_LAKES=0, NUM_MOUNTAINS=0");
        WorldConfig config = new WorldConfig(props);

        return new World(config);
    }

    public Connection getConnection() throws SQLException {
        if (connection == null && dbUrl != null) {
            connection = DriverManager.getConnection(dbUrl);
        }
        return connection;
    }
}