package za.co.wethinkcode.WebServer.DomainLayer;

import za.co.wethinkcode.server.world.World;

public class WorldApi {
    private String worldName;
    private World world;

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String name) {
        this.worldName = name;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Use this convenient factory method to add a world from the database.
     * @param world the world to add
     * @param name the name of the world to be added
     * @return a WorldApi object.
     */
    public static WorldApi addWorldsFromDatabase(World world, String name) {
        WorldApi worldApi = new WorldApi();
        worldApi.setWorld(world);
        worldApi.setWorldName(name);
        return worldApi;
    }
}
