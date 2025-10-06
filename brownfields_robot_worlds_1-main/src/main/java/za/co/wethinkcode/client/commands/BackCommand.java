package za.co.wethinkcode.client.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.server.utils.MovementValidator;
import za.co.wethinkcode.server.utils.RobotDestroyedException;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;

/**
 * A command that moves the robot backward by a specified number of steps.
 * Each step is validated to prevent movement through obstacles.
 */

public class BackCommand implements Command {
    private final World world;
    private final Robot robot;
    private final int steps;

    public BackCommand(World world, Robot robot, int steps) {
        this.world = world;
        this.robot = robot;
        this.steps = steps;
    }

    /**
     * Executes the command by moving the robot backward.
     * Validates each step and returns a response based on success,
     * obstruction, or destruction.
     *
     * @return a JSON string representing the result of the command
     */

    @Override
    public String execute() {
        try {
            Position original = robot.getPosition();
            Position current = original;
            Position next;

            int actualStepsMoved = 0;

            for (int i = 0; i < steps; i++) {
                next = getNextBackwardPosition(current, robot.getDirection());

                if (MovementValidator.isBlocked(next, robot, world)) {
                    if (actualStepsMoved > 0) {
                        robot.setPosition(current);
                        return createPartialMoveResponse();
                    }
                    return createObstructedResponse();
                }

                current = next;
                actualStepsMoved++;
            }

            robot.setPosition(current);
            return actualStepsMoved == steps ? createSuccessResponse() : createPartialMoveResponse();

        } catch (RobotDestroyedException e) {
            return createRobotDestroyedResponse(e.getMessage());
        }
    }


    private Position getNextBackwardPosition(Position position, Direction direction) {
        int x = position.getX();
        int y = position.getY();

        return switch (direction) {
            case NORTH -> new Position(x, y - 1); // subtract instead of add
            case EAST -> new Position(x - 1, y);
            case SOUTH -> new Position(x, y + 1); // add instead of subtract
            case WEST -> new Position(x + 1, y);
            default -> throw new IllegalStateException("Invalid direction");
        };
    }

    private String createSuccessResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Done");
        response.add("data", data);

        response.add("state", new StateCommand(robot).toJson());
        return new Gson().toJson(response);
    }

    private String createPartialMoveResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Obstructed");
        response.add("data", data);

        response.add("state", new StateCommand(robot).toJson());
        return new Gson().toJson(response);
    }

    private String createObstructedResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Obstructed");
        response.add("data", data);

        response.add("state", new StateCommand(robot).toJson());
        return new Gson().toJson(response);
    }

    private String createRobotDestroyedResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", message);
        response.add("data", data);

        // No state included since robot is destroyed
        return new Gson().toJson(response);
    }

    /**
     * Returns the name of the command.
     *
     * @return the string {@code "back"}
     */
    @Override
    public String getName() {
        return "back";
    }

    /**
     * Returns a human-readable description of the command.
     *
     * @return a string describing the backward movement
     */
    @Override
    public String display() {
        return "Move back " + steps + " steps";
    }
}