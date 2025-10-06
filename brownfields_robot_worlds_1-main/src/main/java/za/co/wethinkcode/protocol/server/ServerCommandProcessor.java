package za.co.wethinkcode.protocol.server;

import com.google.gson.*;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.client.commands.StateCommand;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.utils.PositionFinder;
import za.co.wethinkcode.server.utils.VisionFinder;
import za.co.wethinkcode.server.world.World;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The ServerCommandProcessor class handles all incoming commands on the server side.
 * It processes messages from clients, interprets robot and world-related commands, and
 * uses a CommandFactory to create and execute the appropriate actions in the game world.
 */
public class ServerCommandProcessor {
    protected final World world;
    private final Gson gson = new Gson();
    private final CommandFactory commandFactory;
    protected List<Robot> robots = new ArrayList<>();

    /**
     * Creates a new ServerCommandProcessor instance to handle robot and world commands.
     *
     * @param world the game world in which robots operate
     */
    public ServerCommandProcessor(World world) {
        this.world = world;
        this.commandFactory = new CommandFactory(world);
    }

    /**
     * Processes a JSON message from the client and executes the appropriate command.
     */
    public String processMessage(String message) {
        try {
            JsonObject request = gson.fromJson(message, JsonObject.class);

            if (!request.has("command")) {
                return createErrorResponse("Missing command");
            }

            String command = request.get("command").getAsString().toLowerCase();

            // Server administration commands
            return switch (command) {
                case "dump" -> {
                    try {
                        yield commandFactory.createDumpCommand().execute();
                    } catch (SQLException e) {
                        yield createErrorResponse("Failed to dump world: " + e.getMessage());
                    }
                }
                case "robots" -> {
                    try {
                        yield commandFactory.createRobotsCommand().execute();
                    } catch (SQLException e) {
                        yield createErrorResponse("Failed to list robots: " + e.getMessage());
                    }
                }
                default ->
                    // Robot operation commands
                        processRobotCommand(request);
            };

        } catch (JsonSyntaxException e) {
            return createErrorResponse("Invalid JSON format");
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }

    private String processRobotCommand(JsonObject request) {
        Robot robot = world.getRobotByName(request.get("robot").getAsString());

        if (robot != null && robot.getStatus() == Robot.Status.DEAD) {
            return createErrorResponse("Robot is DEAD and cannot execute commands");
        }

        String command = request.get("command").getAsString();
        String robotName = request.get("robot").getAsString();

        switch (command) {
            case "launch":
                return processLaunchCommand(robotName, request);
            case "look":
                return processLookCommand(robotName);
            case "state":
                return processStateCommand(robotName);
            case "forward":
            case "back":
                return processMoveCommand(robotName, request);
            case "turn":
                return processTurnCommand(robotName, request);
            case "fire":
                return processFireCommand(robotName);
            case "reload":
                return processReloadCommand(robotName);
            case "repair":
                return processRepairCommand(robotName);
            default:
                return createErrorResponse("Unsupported command");
        }
    }

    private String processLaunchCommand(String robotName, JsonObject request) {
        try {
            if (!request.has("arguments")) {
                return createErrorResponse("Launch requires arguments: [make]");
            }

            String command = request.get("command").getAsString();
            if (!command.equalsIgnoreCase("launch")) return createErrorResponse("Unsupported command");

            JsonArray args = request.getAsJsonArray("arguments");
            if (args.size() < 1) {
                return createErrorResponse("Launch requires make");
            }

            String make = args.get(0).getAsString();

            // Get values from world config
            int shields = world.getMaxShieldStrength();
            int shots = world.getMaxShots();

            // Check if robot exists
            if (world.getRobotByName(robotName) != null) {
                return createErrorResponse("Too many of you in this world");
            }

            Position pos = new PositionFinder(world).findRandomOpenPosition();
            if (pos == null) {
                return createErrorResponse("No more space in this world");
            }

            Robot robot = new Robot(robotName, pos);
            robot.setMake(make);
            robot.setShields(shields);
            robot.setShots(shots);
            world.addRobot(robot);
            robots.add(robot);

            return createSuccessResponse(robot);
        } catch (Exception e) {
            return createErrorResponse("Invalid launch parameters: " + e.getMessage());
        }
    }

    private String processLookCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        VisionFinder visionFinder = new VisionFinder(world, robot);
        JsonObject visionData = visionFinder.findInAbsoluteDirections(robot);

        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        response.add("data", visionData);
        response.add("state", new StateCommand(robot).toJson());
        return gson.toJson(response);
    }

    private String processStateCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        response.add("state", new StateCommand(robot).toJson());
        return gson.toJson(response);
    }

    // handling movement <forward and back> commands
    private String processMoveCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        int steps = 1;

        JsonArray args = request.getAsJsonArray("arguments");
        if (request.has("arguments")) {
            if (!args.isEmpty()) {
                steps = args.get(0).getAsInt();
            }
        }

        String movementDirection = request.get("command").getAsString(); // accessing 'movementDirection' <forward/back>
        Command command;

        switch (movementDirection) {
            case "forward":
                command = commandFactory.createForwardCommand(robot, steps);
                break;
            case "back":
                command = commandFactory.createBackCommand(robot, steps);
                break;
            default:
                return createErrorResponse("Invalid move. Must be 'forward' or 'back'");
        }
        try {
            return command.execute();
        } catch (SQLException e) {
            return createErrorResponse("Failed to move: " + e.getMessage());
        }
    }

    private String processTurnCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        if (!request.has("arguments")) {
            return createErrorResponse("Turn requires direction argument");
        }

        JsonArray args = request.getAsJsonArray("arguments");
        if (args.size() < 1) {
            return createErrorResponse("Turn requires direction argument");
        }

        String direction = args.get(0).getAsString().toLowerCase();
        Command command;

        switch (direction) {
            case "left":
                command = commandFactory.createTurnLeftCommand(robot);
                break;
            case "right":
                command = commandFactory.createTurnRightCommand(robot);
                break;
            default:
                return createErrorResponse("Invalid direction. Must be 'left' or 'right'");
        }

        try {
            return command.execute();
        } catch (SQLException e) {
            return createErrorResponse("Failed to turn: " + e.getMessage());
        }
    }

    private String processFireCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createFireCommand(robot);
        try {
            return command.execute();
        } catch (SQLException e) {
            return createErrorResponse("Failed to fire: " + e.getMessage());
        }
    }

    private String processReloadCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createReloadCommand(robot);
        try {
            return command.execute();
        } catch (SQLException e) {
            return createErrorResponse("Failed to reload: " + e.getMessage());
        }
    }

    private String processRepairCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createRepairCommand(robot);
        try {
            return command.execute();
        } catch (SQLException e) {
            return createErrorResponse("Failed to repair: " + e.getMessage());
        }
    }

    private String createSuccessResponse(Robot robot) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.add("position", gson.toJsonTree(new int[]{
                robot.getPosition().getX(),
                robot.getPosition().getY()
        }));

        response.add("data", data);
        response.add("state", new StateCommand(robot).toJson());

        return gson.toJson(response);
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "ERROR");

        JsonObject data = new JsonObject();
        data.addProperty("message", message);
        response.add("data", data);
        return gson.toJson(response);
    }

    /**
     * Removes all robots from the world.
     */
    public void removeAllRobots() {
        for (Robot robot : robots) {
            world.removeRobot(robot);
        }
        System.out.println("Robots removed");
    }
}