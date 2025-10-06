package za.co.wethinkcode.client.commands;

import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Direction;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

/**
 * A command that turns the robot 90 degrees to the left (counter-clockwise).
 * Updates the robot's direction and returns the updated state.
 */

public class TurnLeftCommand implements Command {
    private final World world;
    private final Robot robot;
    private final Gson gson = new Gson();

    /**
     * Constructs a TurnLeftCommand for the specified robot and world.
     *
     * @param world the game world
     * @param robot the robot to turn
     */

    public TurnLeftCommand(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
    }

    /**
     * Executes the turn left command.
     * Updates the robot's direction and returns a JSON response.
     *
     * @return JSON string representing the result of the command
     */

    @Override
    public String execute() {
        Direction currentDirection = robot.getDirection();
        Direction newDirection = calculateNewDirection(currentDirection);
        robot.setDirection(newDirection);

        return createResponse(newDirection);
    }

    private Direction calculateNewDirection(Direction currentDirection) {
        return switch (currentDirection) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
        };
    }

    private String createResponse(Direction newDirection) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Done");
        response.add("data", data);

        JsonObject state = new StateCommand(robot).toJson();
        response.add("state", state);

        return gson.toJson(response);
    }

    /**
     * Returns the name of the command.
     *
     * @return "left"
     */

    @Override
    public String getName() {
        return "left";
    }

    /**
     * Returns a description of the command.
     *
     * @return a string indicating the robot has turned left
     */

    @Override
    public String display() {
        return "Turned left";
    }
}