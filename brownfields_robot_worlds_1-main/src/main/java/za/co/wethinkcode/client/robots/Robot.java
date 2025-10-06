package za.co.wethinkcode.client.robots;

import za.co.wethinkcode.server.model.Direction;

/**
 * Represents a robot in the client-side view of the Robot World Simulation.
 * Stores immutable data such as name, make, position, and facing direction.
 */

public class Robot {
    private final String name;
    private final String make;
    private final int x;
    private final int y;
    private final Direction direction;

    /**
     * Constructs a new {@code Robot} with the given properties.
     *
     * @param name      the robot's name
     * @param make      the robot's make or model
     * @param x         x-coordinate of the robot's position
     * @param y         y-coordinate of the robot's position
     * @param direction the direction the robot is facing
     */

    public Robot(String name, String make, int x, int y, Direction direction) {
        this.name = name;
        this.make = make;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    /**
     * Returns a string representation of the robot's state.
     *
     * @return formatted string describing the robot
     */

    @Override
    public String toString() {
        return String.format("Robot[name=%s, make=%s, position=(%d,%d), facing=%s]",
                name, make, x, y, direction);
    }

    /**
     * Gets the robot's name.
     *
     * @return the name of the robot
     */

    public String getName() {
        return name;
    }

    /**
     * Gets the robot's make or model.
     *
     * @return the make of the robot
     */

    public String getMake() {
        return make;
    }

    /**
     * Gets the robot's current x-coordinate.
     *
     * @return the x-coordinate
     */

    public int getX() {
        return x;
    }

    /**
     * Gets the robot's current y-coordinate.
     *
     * @return the y-coordinate
     */

    public int getY() {
        return y;
    }

    /**
     * Gets the direction the robot is currently facing.
     *
     * @return the robot's direction
     */

    public Direction getDirection() {
        return direction;
    }
}