package za.co.wethinkcode.server.persistence;

import za.co.wethinkcode.server.entities.SavedWorld;
import za.co.wethinkcode.server.entities.WorldProperty;
import za.co.wethinkcode.server.entities.ObstacleType;

import java.sql.SQLException;
import java.util.List;

public interface WorldDAOInterface {
    /**
     * Fetch world size by name.
     */
    int getWorldSizeByName(String worldName) throws SQLException;

    /**
     * Fetch obstacles for a specific world.
     */
    List<WorldProperty> getObstaclesForWorld(String worldName) throws SQLException;

    /**
     * Save a new world to DB.
     */
    void saveWorld(SavedWorld world) throws SQLException;

    /**
     * Save obstacles for a world.
     */
    void saveObstacles(int worldId, List<WorldProperty> obstacles) throws SQLException;

    List<WorldProperty> getAllWorldsWithProperties() throws SQLException;


}
