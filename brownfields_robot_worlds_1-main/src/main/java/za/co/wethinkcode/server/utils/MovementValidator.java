package za.co.wethinkcode.server.utils;

import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;
import za.co.wethinkcode.server.world.obstacles.Pit;

/**
 * Utility class for validating robot movement in the world.
 * Determines whether a robot can move to a given position
 * based on world bounds, obstacles, pits, and other robots.
 */
public class MovementValidator {

    /**
     * Checks whether a given position is blocked for a specific robot.
     * A position may be blocked if it is out of bounds, occupied by
     * an obstacle, pit, or another robot.
     * <p>
     * If the robot steps into a pit, it is removed from the world and a
     * {@link RobotDestroyedException} is thrown.
     *
     * @param position the position the robot wants to move to
     * @param robot    the robot attempting the move
     * @param world    the world context containing obstacles and robots
     * @return {@code true} if the position is blocked; {@code false} otherwise
     * @throws RobotDestroyedException if the robot falls into a pit
     */
    public static boolean isBlocked(Position position, Robot robot, World world) {
        // Check world boundaries first
        if (!world.isPositionValid(position)) {
            return true;
        }

        // Check for pits (which destroy the robot)
        for (Obstacle obstacle : world.getObstacles()) {
            if (obstacle instanceof Pit && obstacle.blocksPosition(position.getX(), position.getY())) {
                world.removeRobot(robot);
                throw new RobotDestroyedException("Fell into a bottomless pit");
            }
        }

        // Check for other obstacles
        for (Obstacle obstacle : world.getObstacles()) {
            if (obstacle.blocksPosition(position.getX(), position.getY())) {
                return true;
            }
        }

        // Check for other robots (movement-specific)
        for (Robot otherRobot : world.getRobots()) {
            if (!otherRobot.equals(robot) &&
                    otherRobot.getPosition().equals(position)) {
                return true;
            }
        }

        return false;
    }
}