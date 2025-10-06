package za.co.wethinkcode.server.world.obstacles;

import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;

/**
 * Represents a Mountain obstacle in the world.
 * Mountains block visibility and movement.
 */
public class Mountain extends Obstacle {

    /**
     * Constructs a Mountain obstacle at the specified coordinates and size.
     *
     * @param x      The x-coordinate of the obstacle's top-left corner.
     * @param y      The y-coordinate of the obstacle's top-left corner.
     * @param width  The width of the mountain.
     * @param height The height of the mountain.
     */

    public Mountain(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

//    /**
//     @param position The top-left position of the mountain.
//     */
//    public Mountain(Position position) {
//        super(position.getX(), position.getY(), 1, 1);
//    }

    @Override
    public String getType() {
        return "Mountain";
    }

    /**
     * Indicates whether the mountain blocks visibility.
     *
     * @return true, as mountains block visibility.
     */
    @Override
    public boolean blocksVisibility() {
        return true;
    }

    /**
     * Indicates whether movement is allowed in the specified direction.
     *
     * @param direction The direction of intended movement.
     * @return false, mountains block movement in all directions.
     */
    public boolean canMove(Direction direction) {
        return false;
    }

    /**
     * Returns a string representation of the Mountain object.
     *
     * @return A string describing the mountain and its coordinates.
     */
    @Override
    public String toString() {
        return String.format("Mountain (%3d, %3d)", getX(), getY());
    }
}
