package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.server.ServerConsole;
import za.co.wethinkcode.server.persistence.WorldDao;
import za.co.wethinkcode.server.RobotWorldServer;
import za.co.wethinkcode.server.world.World;

import java.sql.SQLException;

/**
 * Command to restore the game world from the database.
 */
public class RestoreCommand implements Command {
    private final WorldDao worldDao;

    /**
     * Constructs a RestoreCommand with the specified WorldDao.
     *
     * @param worldDao the data access object for world restoration
     */
    public RestoreCommand(WorldDao worldDao) {
        this.worldDao = worldDao;
    }

    /**
     * Executes the restore command, loading the world from the database.
     *
     * @return a success message
     * @throws SQLException if a database error occurs
     */
    @Override
    public String execute() throws SQLException {
        World restoredWorld = worldDao.restoreWorld();
        RobotWorldServer.setWorld(restoredWorld);
        return "✅ World restored successfully";
    }

    /**
     * Returns the name of the command.
     *
     * @return the string "restore"
     */
    @Override
    public String getName() {
        return "restore";
    }

    /**
     * Returns a displayable representation of the command result.
     *
     * @return a success message
     */
    @Override
    public String display() {
        return "✅ World restored successfully";
    }
}