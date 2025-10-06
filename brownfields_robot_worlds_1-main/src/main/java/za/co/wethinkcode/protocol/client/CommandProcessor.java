package za.co.wethinkcode.protocol.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.client.Play;

/**
 * Parses and validates client string commands and converts them to JSON
 * commands to be sent to the server.
 * Works in coordination with the Play class to ensure correct robot context.
 */

public class CommandProcessor {
    private final Play play;
    private final Gson gson = new Gson();
    private String selectedMake;
    private String robotName;

    /**
     * Constructs a CommandProcessor linked to the main game Play instance.
     *
     * @param play the Play object managing the game session
     */
    public CommandProcessor(Play play) {
        this.play = play;
    }

    /**
     * Sets the robot make and name expected for command validation.
     *
     * @param make the robot make/model
     * @param name the robot's name
     */
    public void setRobotDetails(String make, String name) {
        this.selectedMake = make;
        this.robotName = name;
    }

    /**
     * Converts a string command input to a JSON command string if valid.
     * Returns an error JSON if invalid or unknown command.
     *
     * @param input the raw command string from the user
     * @return JSON-formatted command string or error response
     */

    public String convertToJsonCommand(String input) {
        String[] parts = input.trim().split(" ");
        if (parts.length < 1) {
            return createErrorResponse("Invalid command format");
        }

        String command = parts[0].toLowerCase();

        switch (command) {
            case "launch":
                return processLaunchCommand(parts);
            case "look":
                return processLookCommand(parts);
            case "state":
                return processStateCommand(parts);
            case "forward":
                return processMoveCommand(parts, command);
            case "back":
                return processMoveCommand(parts, command);
            case "turn":
                return processTurnCommand(parts);
            case "fire":
                return processFireCommand(parts);
            case "reload":
                return processReloadCommand(parts);
            case "repair":
                return processRepairCommand(parts);
            default:
                return createErrorResponse("Unknown command");
        }
    }

    private String processLaunchCommand(String[] parts) {
        if (parts.length != 3) {
            return createErrorResponse("Launch requires make and name");
        }

        String make = parts[1];
        String name = parts[2];

        if (!make.equalsIgnoreCase(selectedMake)) {
            return createErrorResponse("Invalid make. You must use: " + selectedMake);
        }
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        JsonObject command = new JsonObject();
        command.addProperty("command", "launch");
        command.addProperty("robot", name);

        JsonArray args = new JsonArray();
        args.add(make);
        // Shields and shots will be set by server from config
        command.add("arguments", args);

        return gson.toJson(command);
    }

    private String processLookCommand(String[] parts) {
        if (parts.length < 2) {
            return createErrorResponse("Look requires robot name");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        // return helper method processClientCommands()
        return processClientCommands(parts, "look");
    }

    private String processStateCommand(String[] parts) {
        if (parts.length < 2) {
            return createErrorResponse("State requires robot name");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        // return helper method processClientCommands()
        return processClientCommands(parts, "state");
    }

    /**
     * Checks the length of the string command input.
     * Returns an error JSON if string < 2, since we expect (forward/back [robot] [steps]).
     *
     * @param parts the raw command string array from the user
     * @param direction the raw command string stripped from parts
     * @return JSON-formatted command string or error response
     */
    private String processMoveCommand(String[] parts, String direction) {
        if (parts.length < 2) {
            return createErrorResponse(direction.substring(0, 1).toUpperCase() + direction.substring(1) + " requires robot name");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        JsonObject command = new JsonObject();
        command.addProperty("command", direction);
        command.addProperty("robot", name);

        JsonArray args = new JsonArray();
        if (parts.length > 2) {
            try {
                int step = Integer.parseInt(parts[2]);
                args.add(step);
            } catch (NumberFormatException e) {
                return createErrorResponse("Invalid number of arguments");
            }
        } else {
            args.add(1);
        }

        command.add("arguments", args);
        return gson.toJson(command);
    }

    private String processTurnCommand(String[] parts) {
        if (parts.length < 3) {
            return createErrorResponse("Turn requires robot name and direction");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        String direction = parts[2].toLowerCase();
        if (!direction.equals("left") && !direction.equals("right")) {
            return createErrorResponse("Direction must be 'left' or 'right'");
        }

        JsonObject command = new JsonObject();
        command.addProperty("command", "turn");
        command.addProperty("robot", name);

        JsonArray args = new JsonArray();
        args.add(direction);
        command.add("arguments", args);

        return gson.toJson(command);
    }

    private String processFireCommand(String[] parts) {
        if (parts.length < 2) {
            return createErrorResponse("Fire requires robot name");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        // return helper method processClientCommands()
        return processClientCommands(parts, "fire");
    }

    private String processReloadCommand(String[] parts) {
        if (parts.length < 2) {
            return createErrorResponse("Reload requires robot name");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        // return helper method processClientCommands()
        return processClientCommands(parts, "reload");
    }

    private String processRepairCommand(String[] parts) {
        if (parts.length < 2) {
            return createErrorResponse("Repair requires robot name");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        // return helper method processClientCommands()
        return processClientCommands(parts, "repair");
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "ERROR");
        response.addProperty("message", message);
        return gson.toJson(response);
    }

    // helper method to process commands, reduces redundancy
    private String processClientCommands(String[] parts, String commandName) {
        if (parts.length < 2) {
            return createErrorResponse(commandName.substring(0, 1).toUpperCase() + commandName.substring(1) + " requires robot name");
        }

        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid bot name, use: " + robotName);
        }

        JsonObject jsonCommand = new JsonObject();
        jsonCommand.addProperty("command", commandName);
        jsonCommand.addProperty("robot", name);
        jsonCommand.add("arguments", new JsonArray());

        return gson.toJson(jsonCommand);
    }
}