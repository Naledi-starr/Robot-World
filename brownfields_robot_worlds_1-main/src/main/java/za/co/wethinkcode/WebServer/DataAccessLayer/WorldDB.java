package za.co.wethinkcode.WebServer.DataAccessLayer;

import za.co.wethinkcode.WebServer.DomainLayer.WorldApi;
import za.co.wethinkcode.server.world.World;

import java.io.IOException;

public interface WorldDB {
    /**
     * Get a single world by name.
     * @param worldName the name of the world
     * @return a World
     */
    World getSPecificWorld(String worldName);

    /**
     * Get the current world
     *
     * @return A list of quotes
     */
    String getCurrentWorld(World world);

    /**
     * Add current world to the database ( Api databse
     * @param world world to add
     * @return the newly added World
     */
    WorldApi addWorld(World world) throws IOException;
}
