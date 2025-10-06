package za.co.wethinkcode.server.model;

import java.util.Objects;

/**
 * Immutable 2D coordinate representing a position in the robot world.
 * Used for locating entities like robots and obstacles.
 */
public class Position {
    private final int x;
    private final int y;

    /**
     * Creates a new {@code Position} with the given coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate.
     *
     * @return x value
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate.
     *
     * @return y value
     */
    public int getY() {
        return y;
    }

    /**
     * Compares this position to another for equality.
     *
     * @param o the object to compare
     * @return {@code true} if x and y match, else {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position position)) return false;
        return x == position.x && y == position.y;
    }

    /**
     * Returns the hash code of this position.
     *
     * @return hash code based on x and y
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Returns this position as a string in format {@code (x,y)}.
     *
     * @return string representation of this position
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
