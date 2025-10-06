package za.co.wethinkcode.server.world.obstacles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates random obstacles within a defined world area.
 * Ensures obstacles do not overlap and are within world boundaries.
 */
public class ObstacleGenerator {
    private final int worldWidth;
    private final int worldHeight;
    private final int minSize;
    private final int maxSize;
    private final Random random = new Random();

    /**
     * Creates an ObstacleGenerator for a world of specified dimensions
     * and obstacle size constraints.
     *
     * @param worldWidth  The width of the world.
     * @param worldHeight The height of the world.
     * @param minSize     The minimum width/height of obstacles.
     * @param maxSize     The maximum width/height of obstacles.
     */
    public ObstacleGenerator(int worldWidth, int worldHeight, int minSize, int maxSize) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    /**
     * Generates a list of randomly sized and positioned obstacles.
     * Obstacles will not overlap and will fit within the world boundaries.
     *
     * @param numObstacles The number of obstacles to generate.
     * @return A list of generated obstacles.
     */
    public List<Obstacle> generateObstacles(int numObstacles) {
        List<Obstacle> obstacles = new ArrayList<>();

        while (obstacles.size() < numObstacles) {
            int width = random.nextInt(maxSize - minSize + 1) + minSize;
            int height = random.nextInt(maxSize - minSize + 1) + minSize;

            int x = random.nextInt(worldWidth - width + 1);
            int y = random.nextInt(worldHeight - height + 1);

            Obstacle newObstacle = createRandomObstacle(x, y, width, height);

            boolean overlaps = obstacles.stream().anyMatch(o -> overlaps(o, newObstacle));

            if (!overlaps) {
                obstacles.add(newObstacle);
            }
        }

        return obstacles;
    }

    private Obstacle createRandomObstacle(int x, int y, int width, int height) {
        // 0 = Mountain, 1 = Lake, 2 = Pit
        int type = random.nextInt(3);

        return switch (type) {
            case 0 -> new Mountain(x, y, width, height);
            case 1 -> new   Lake(x, y, width, height);
            case 2 -> new Pit(x, y, width, height);
            default -> throw new IllegalStateException("Unexpected obstacle type: " + type);
        };
    }

    private boolean overlaps(Obstacle a, Obstacle b) {
        return a.getX() < b.getX() + b.getWidth() &&
                a.getX() + a.getWidth() > b.getX() &&
                a.getY() < b.getY() + b.getHeight() &&
                a.getY() + a.getHeight() > b.getY();
    }
}
