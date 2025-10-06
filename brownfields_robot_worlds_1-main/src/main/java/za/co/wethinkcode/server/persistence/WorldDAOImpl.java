package za.co.wethinkcode.server.persistence;

import za.co.wethinkcode.server.entities.SavedWorld;
import za.co.wethinkcode.server.entities.WorldProperty;
import za.co.wethinkcode.server.entities.ObstacleType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorldDAOImpl implements WorldDAOInterface {
    private final Connection connection;

    public WorldDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int getWorldSizeByName(String worldName) throws SQLException {
        String sql = "SELECT world_size FROM saved_worlds WHERE world_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, worldName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("world_size");
            }
            throw new SQLException("No world found with name: " + worldName);
        }
    }

    @Override
    public List<WorldProperty> getObstaclesForWorld(String worldName) throws SQLException {
        String sql = "SELECT o.id, o.obstacle_name, ws.x_cord, ws.y_cord " +
                "FROM world_properties ws " +
                "JOIN obstacles o ON ws.obstacle_id = o.id " +
                "WHERE ws.world_id = (SELECT id FROM saved_worlds WHERE world_name = ? LIMIT 1)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, worldName);
            ResultSet rs = pstmt.executeQuery();

            List<WorldProperty> obstacles = new ArrayList<>();
            while (rs.next()) {
                ObstacleType obstacleType = new ObstacleType();
                obstacleType.setId(rs.getInt("id"));
                obstacleType.setName(rs.getString("obstacle_name"));

                obstacles.add(new WorldProperty(
                        obstacleType,
                        rs.getInt("x_cord"),
                        rs.getInt("y_cord")
                ));
            }
            return obstacles;
        }
    }


    @Override
    public void saveWorld(SavedWorld world) throws SQLException {
        String sql = "INSERT INTO saved_worlds (world_name, world_size) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, world.getWorldName());
            pstmt.setInt(2, world.getWorldSize());
            pstmt.setTimestamp(3, Timestamp.valueOf(world.getCreatedAt()));
            pstmt.executeUpdate();
        }
    }

    @Override
    public void saveObstacles(int worldId, List<WorldProperty> obstacles) throws SQLException {
        String sql = "INSERT INTO world_properties (world_id, obstacle_id, x_cord, y_cord) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (WorldProperty wp : obstacles) {
                pstmt.setInt(1, worldId);
                pstmt.setInt(2, wp.getObstacleType().getId());
                pstmt.setInt(3, wp.getX());
                pstmt.setInt(4, wp.getY());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    @Override
    public List<WorldProperty> getAllWorldsWithProperties() throws SQLException {
        return List.of();
    }
}
