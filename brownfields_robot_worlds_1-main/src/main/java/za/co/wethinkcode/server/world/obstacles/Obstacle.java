package za.co.wethinkcode.server.world.obstacles;

/**
 * Abstract representation of an obstacle in the world.
 * Each obstacle has a position and dimensions defined by its coordinates, width, and height.
 * Subclasses must specify the obstacle type and visibility behavior.
 *
 * @author Thendo and Lindokuhle
 */
public abstract class Obstacle {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /**
     * Indicates if this obstacle blocks visibility.
     *
     * @return true if visibility is blocked, false otherwise.
     */
    public abstract boolean blocksVisibility();

    /**
     * Constructs an obstacle at the specified position with given dimensions.
     *
     * @param x The x-coordinate of the top-left corner.
     * @param y The y-coordinate of the top-left corner.
     * @param width The width of the obstacle.
     * @param height The height of the obstacle.
     */
    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Returns the x-coordinate of the obstacle's top-left corner. */
    public int getX() {
        return x;
    }

    /** Returns the y-coordinate of the obstacle's top-left corner. */
    public int getY() {
        return y;
    }

    /** Returns the x-coordinate of the bottom-right corner. */
    public int getBottomRightX() {
        return x + width - 1;
    }

    /** Returns the y-coordinate of the bottom-right corner. */
    public int getBottomRightY() {
        return y + height - 1;
    }

    /** Returns the width of the obstacle. */
    public int getWidth() {
        return width;
    }

    /** Returns the height of the obstacle. */
    public int getHeight() {
        return height;
    }

    /**
     * Determines whether this obstacle blocks the specified position.
     *
     * @param posX The x-coordinate to check.
     * @param posY The y-coordinate to check.
     * @return true if the position lies within the obstacle boundaries.
     */
    public boolean blocksPosition(int posX, int posY) {
        return posX >= x && posX < x + width && posY >= y && posY < y + height;
    }

    /** Returns the x-coordinate of the bottom-left corner. */
    public int getBottomLeftX() {
        return x;
    }

    /** Returns the y-coordinate of the bottom-left corner. */
    public int getBottomLeftY() {
        return y + height;
    }

    /**
     * Returns the type of this obstacle.
     *
     * @return A string representing the obstacle type.
     */
    public abstract String getType();
}
