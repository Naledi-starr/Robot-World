package za.co.wethinkcode.server.model;

import java.util.Objects;

/**
 * Represents a robot entity in the world, holding attributes like name, position,
 * direction, shields, shots, make, and status. A robot can move, interact, and take damage.
 */
public class Robot {
    private final String name;
    private String make;
    private Position position;
    private Direction direction;
    private int shields;
    private int shots;
    private Status status = Status.NORMAL;

    /**
     * Enumeration of possible robot statuses.
     */
    public enum Status {
        NORMAL, RELOAD, REPAIR, DEAD
    }

    /**
     * Creates a robot with the specified name and initial position.
     * Default direction is {@code NORTH}.
     *
     * @param name     robot's name
     * @param position initial position
     */
    public Robot(String name, Position position) {
        this.name = name;
        this.direction = Direction.NORTH;
        this.position = position;
    }

    /** @return the robot's name */
    public String getName() {
        return name;
    }

    /** @return the robot's make/model */
    public String getMake() {
        return make;
    }

    /**
     * Sets the robot's make/model.
     *
     * @param make the make or type identifier
     */
    public void setMake(String make) {
        this.make = make;
    }

    /** @return the current position of the robot */
    public Position getPosition() {
        return position;
    }

    /**
     * Updates the robot's position.
     *
     * @param position new position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /** @return the direction the robot is facing */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the direction the robot is facing.
     *
     * @param direction new direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /** @return the number of shields remaining */
    public int getShields() {
        return shields;
    }

    /**
     * Sets the number of shields. If below 0, robot status becomes {@code DEAD}.
     *
     * @param shields new shield value
     */
    public void setShields(int shields) {
        this.shields = shields;
        if (this.shields < 0) {
            this.shields = 0;
            this.status = Status.DEAD;
        }
    }

    /** @return the number of shots remaining */
    public int getShots() {
        return shots;
    }

    /**
     * Sets the number of shots.
     *
     * @param shots new shot count
     */
    public void setShots(int shots) {
        this.shots = shots;
    }

    /** @return current status of the robot */
    public Status getStatus() {
        return status;
    }

    /**
     * Updates the robot's status.
     *
     * @param status new status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Applies damage to the robot, reducing shields by one.
     * If shields fall below zero, robot is marked as {@code DEAD}.
     *
     * @return {@code true} if robot died from this hit, {@code false} otherwise
     */
    public boolean takeHit() {
        if (this.status == Status.DEAD) {
            return true;
        }

        this.shields--;

        if (this.shields < 0) {
            this.shields = 0;
            this.status = Status.DEAD;
            return true;
        }
        return false;
    }

    /**
     * Checks if the robot is still operational.
     *
     * @return {@code true} if not {@code DEAD}, else {@code false}
     */
    public boolean isAlive() {
        return this.status != Status.DEAD;
    }

    /** @inheritDoc */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Robot)) return false;
        Robot robot = (Robot) o;
        return getName().equals(robot.getName());
    }

    /** @inheritDoc */
    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    /**
     * Returns a summary of the robot's name, make, position, and direction.
     *
     * @return string representation of the robot
     */
    @Override
    public String toString() {
        return "Robot[name=" + name +
                ", make=" + make +
                ", position=" + (position != null ? position : "(not set)") +
                ", direction=" + direction + "]";
    }
}
