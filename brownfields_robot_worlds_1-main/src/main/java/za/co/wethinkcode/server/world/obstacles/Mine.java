package za.co.wethinkcode.server.world.obstacles;

/**
 * Represents a mine obstacle in the game world.
 */
public class Mine extends Obstacle {
    /**
     * Constructs a Mine at the specified position with given dimensions.
     *
     * @param x      the x-coordinate
     * @param y      the y-coordinate
     * @param width  the width of the mine
     * @param height the height of the mine
     */
    public Mine(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Returns the type of the obstacle.
     *
     * @return the string "Mine"
     */
    @Override
    public String getType() {
        return "Mine";
    }

    /**
     * Indicates whether the mine blocks visibility.
     *
     * @return true, as mines block visibility
     */
    @Override
    public boolean blocksVisibility() {
        return true;
    }

    /**
     * Returns a string representation of the mine.
     *
     * @return a string describing the mine's position
     */
    @Override
    public String toString() {
        return String.format("Mine (%3d, %3d)", getX(), getY());
    }
}