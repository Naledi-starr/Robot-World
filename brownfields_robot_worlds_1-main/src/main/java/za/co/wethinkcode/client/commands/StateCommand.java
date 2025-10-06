package za.co.wethinkcode.client.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;

/**
 * Provides the current state of a robot, including position, direction, shields, and shots.
 * Used for JSON responses and formatted output.
 */

public class StateCommand {
    private final Position position;
    private final Direction direction;
    private final int shields;
    private final int shots;
    private final String status;

    /**
     * Constructs a StateCommand from the given robot's current state.
     *
     * @param robot the robot whose state is represented
     */

    public StateCommand(Robot robot) {
        this.position = robot.getPosition();
        this.direction = robot.getDirection();
        this.shields = robot.getShields();
        this.shots = robot.getShots();
        this.status = "NORMAL";
    }

    /**
     * Returns the robot's state as a JSON object.
     *
     * @return JSON representation of the robot's state
     */

    public JsonObject toJson() {
        JsonObject stateJson = new JsonObject();
        stateJson.add("position", new Gson().toJsonTree(new int[]{position.getX(), position.getY()}));
        stateJson.addProperty("direction", direction.name());
        stateJson.addProperty("shields", shields);
        stateJson.addProperty("shots", shots);
        stateJson.addProperty("status", status);
        return stateJson;
    }

    /**
     * Returns the robot's current status.
     *
     * @return the status string
     */

    public String getStatus() {
        return this.status;
    }

    /**
     * Returns a short-form version of the robot's direction.
     *
     * @return a single-letter direction (N, E, S, W)
     */

    public String getDirectionShort() {
        return switch (direction) {
            case NORTH -> "N";
            case EAST -> "E";
            case SOUTH -> "S";
            case WEST -> "W";
            default -> direction.name();
        };
    }
}