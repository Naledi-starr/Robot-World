package za.co.wethinkcode.WebServer.DataAccessLayer;

import za.co.wethinkcode.WebServer.DomainLayer.WorldApi;
import za.co.wethinkcode.server.commands.SaveCommand;
import za.co.wethinkcode.server.persistence.WorldDao;
import za.co.wethinkcode.server.world.World;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class TestDatabase implements WorldDB {
    private Map<String, World> worlds;
    private final String dbUrl = "jdbc:sqlite:robot-worlds.db";
    private WorldDao worldDao = new WorldDao(dbUrl);
    private SaveCommand saveCommand;

    public TestDatabase() throws SQLException, IOException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:robot-worlds.db");
        worlds = worldDao.worldsInDatabase(conn);
        for (String worldName: worldNamesInDatabase()){
            World world = worldDao.restoreWorldByName(worldName);
            this.addWorld(world);
        }
    }

    @Override
    public World getSPecificWorld(String worldName) {
        try {
            World world = worldDao.restoreWorldByName(worldName);
            return world;
        } catch (SQLException e) {
            System.out.println("World not found. Exception was made.");
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getCurrentWorld(World world) {
        return world.displayJson();
    }


    @Override
    public WorldApi addWorld(World world) throws IOException {
        saveCommand = new SaveCommand(world);
        String worldName = "world1";
        int x = 1;
        String sql = "SELECT world_name FROM saved_worlds";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:robot-worlds.db")) {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ResultSet rs = stmt.executeQuery();;
                while (rs.next()){
                    String name = rs.getString("world_name");
                    if (worldName.equals(name)){
                        worldName = saveCommand.generateNextDefaultName(conn);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to get world names: " + e.getMessage());
        }
        WorldApi worldApi = new WorldApi();
        worldApi.setWorldName(worldName);
        worldApi.setWorld(world);
        return worldApi;
    }

    public List<String> worldNamesInDatabase() throws SQLException {
        List<String> worldNames = new ArrayList<>();
        String sql = "SELECT world_name FROM saved_worlds";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:robot-worlds.db")) {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ResultSet rs = stmt.executeQuery();;
                while (rs.next()){
                    String worldName = rs.getString("world_name");
                    worldNames.add(worldName);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to get world names: " + e.getMessage());
        }
        return worldNames;
    }
}
