package za.co.wethinkcode.server.utils;


import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

/**
 * Utility class for validating if a shot is blocked before reaching a target.
 */
public class ShootingValidator {

    /**
     * Determines if a shot is blocked at the specified position.
     *
     * @param position The target position to check.
     * @param world The current game world containing obstacles and boundaries.
     * @return true if a shot is blocked at the position; false otherwise.
     */
    public static boolean isBlocked(Position position, World world) {
        // Check world boundaries
        if (!world.isPositionValid(position)) {
            return true;
        }

        // Check for obstacles that block shots (like mountains)
        for (Obstacle obstacle : world.getObstacles()) {
            if (obstacle.blocksPosition(position.getX(), position.getY())) {
                return true;
            }
        }

        // Don't check for other robots here - they should be hittable
        return false;
    }
}
