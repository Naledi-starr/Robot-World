package za.co.wethinkcode.server.world.obstacles;

/**
 * Represents a bottomless pit obstacle in the world.
 * A pit does not block visibility but destroys robots that enter its area.
 */
public class Pit extends Obstacle {

    /**
     * Constructs a Pit obstacle at the specified position and size.
     *
     * @param x      The x-coordinate of the pit's top-left corner.
     * @param y      The y-coordinate of the pit's top-left corner.
     * @param width  The width of the pit.
     * @param height The height of the pit.
     */
    public Pit(int x, int y, int width, int height) {
        super(x, y, width, height);
    }


    /**
     * Returns the type of the obstacle.
     *
     * @return The string "BottomlessPit".
     */
    @Override
    public String getType() {
        return "BottomlessPit";
    }

    /**
     * Indicates whether this obstacle blocks visibility.
     *
     * @return false, as pits do not block visibility.
     */
    @Override
    public boolean blocksVisibility() {
        return false;
    }

    /**
     * Returns the x-coordinate of the pit's bottom-left corner.
     *
     * @return The x-coordinate of the bottom-left corner.
     */
    public int getBottomLeftX() {
        return getX();
    }

    /**
     * Returns the y-coordinate of the pit's bottom-left corner.
     *
     * @return The y-coordinate of the bottom-left corner.
     */
    public int getBottomLeftY() {
        return getY() + getHeight();
    }
}
