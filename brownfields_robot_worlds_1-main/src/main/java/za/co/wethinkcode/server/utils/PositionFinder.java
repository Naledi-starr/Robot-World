package za.co.wethinkcode.server.utils;

import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.util.Random;

/**
 * Utility class to find a valid, unoccupied position in the game world.
 * This is typically used when spawning new robots into the world.
 */
public class PositionFinder {
    private final World world;
    private final Random random = new Random();

    /**
     * Constructs a PositionFinder for a given world.
     *
     * @param world the world in which to find open positions
     */
    public PositionFinder(World world) {
        this.world = world;
    }

    /**
     * Attempts to find a random open position within the world.
     * It checks that the position is not occupied by any robot or obstacle,
     * and that it is within the world's valid bounds.
     *
     * @return a valid {@link Position} or {@code null} if no open position was found after 100 attempts
     */
    public Position findRandomOpenPosition() {
        int width = world.getWidth();
        int height = world.getHeight();
        int attempts = 100;

        if (world.getRobots().isEmpty()){
            Position pos = new Position(0, 0);

            if (!isPositionOccupied(pos) && world.isPositionValid(pos)) {
                return pos;
            }

        } else {
            for (int i = 0; i < attempts; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                Position pos = new Position(x, y);

                if (!isPositionOccupied(pos) && world.isPositionValid(pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a given position is already occupied by another robot or obstacle.
     *
     * @param pos the position to check
     * @return true if the position is occupied, false otherwise
     */
    private boolean isPositionOccupied(Position pos) {
        // Check against other robots
        for (Robot robot : world.getRobots()) {
            if (robot.getPosition().equals(pos)) {
                return true;
            }
        }

        // Check against obstacles
        for (Obstacle obstacle : world.getObstacles()) {
            if (obstacle.blocksPosition(pos.getX(), pos.getY())) {
                return true;
            }
        }
        return false;
    }
}