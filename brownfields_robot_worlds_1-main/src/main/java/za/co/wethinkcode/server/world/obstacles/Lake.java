package za.co.wethinkcode.server.world.obstacles;

/**
 * Represents a Lake obstacle in the world.
 * Lakes do not block visibility but occupy space and block movement.
 */
public class Lake extends Obstacle {

    /**
     * Constructs a Lake at the specified position with given dimensions.
     *
     * @param x      The x-coordinate of the top-left corner of the lake.
     * @param y      The y-coordinate of the top-left corner of the lake.
     * @param width  The width of the lake.
     * @param height The height of the lake.
     */
    public Lake(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Returns the type of the obstacle as a string.
     *
     * @return "Lake"
     */
    @Override
    public String getType() {
        return "Lake";
    }

    /**
     * Indicates whether this obstacle blocks vision.
     * Lakes do not block visibility.
     *
     * @return false
     */
    @Override
    public boolean blocksVisibility() {
        return false;
    }
}
