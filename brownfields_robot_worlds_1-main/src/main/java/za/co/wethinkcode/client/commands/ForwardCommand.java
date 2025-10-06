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
 * A command that moves the robot forward in its current direction.
 * Validates each step and handles obstructions or destruction.
 */

public class ForwardCommand implements Command {
    private final World world;
    private final Robot robot;
    private final int steps;

    /**
     * Constructs a {@code ForwardCommand}.
     *
     * @param world the world in which the robot operates
     * @param robot the robot executing the command
     * @param steps the number of steps to move forward
     */

    public ForwardCommand(World world, Robot robot, int steps) {
        this.world = world;
        this.robot = robot;
        this.steps = steps;
    }

    /**
     * Executes the forward command.
     * Moves the robot step-by-step, checking for obstacles.
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
                next = getNextForwardPosition(current, robot.getDirection());

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


    private Position getNextForwardPosition(Position position, Direction direction) {
        int x = position.getX();
        int y = position.getY();

        return switch (direction) {
            case NORTH -> new Position(x, y + 1); // add instead of subtract
            case EAST -> new Position(x + 1, y);
            case SOUTH -> new Position(x, y - 1); // subtract instead of add
            case WEST -> new Position(x - 1, y);
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
     * @return the string {@code "forward"}
     */

    @Override
    public String getName() {
        return "forward";
    }

    /**
     * Returns a human-readable description of the command.
     *
     * @return a string describing the forward movement
     */

    @Override
    public String display() {
        return "Move forward " + steps + " steps";
    }
}