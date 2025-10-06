package za.co.wethinkcode.HTTPClient;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

import java.util.List;
import java.util.Scanner;

public class RobotWorldHTTP {
    private static final String BASE_URL = "http://localhost:5007/";

    private static void getWorldRequest(String worldName) {
        HttpResponse<String> response = Unirest.get(BASE_URL + "world/" + worldName)
                .asString();
        printResponse(response);
    }

    private static void getCurrentWorld() {
        HttpResponse<String> response = Unirest.get(BASE_URL + "world")
                .asString();
        printResponse(response);
    }

    private static void launchRobot(String robotName) {
        HttpResponse<String> response = Unirest.post(BASE_URL + "robot/" + robotName)
                .asString();
        printResponse(response);
    }

    private static void processRobotCommand(String robotName, String command) {
        HttpResponse<String> response = Unirest.post(BASE_URL + "robot/" + robotName + "/" + command)
                .asString();
        printResponse(response);
    }

    private static void processRobotCommandWithArgument(String robotName, String command, String argument) {
        HttpResponse<String> response = Unirest.post(BASE_URL + "robot/" + robotName + "/" + command + "/" + argument)
                .asString();
        printResponse(response);
    }

    private static void printResponse(HttpResponse<String> response) {
        System.out.println("Response Status: " + response.getStatus() + " " + response.getStatusText());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Body: " + response.getBody());
        System.out.println("---------------------------------------------");
    }

    public static void main(String[] args) {
        displayMenu();
        processUserInput();
    }

    private static void displayMenu() {
        System.out.println("---------------------------------------------");
        System.out.println("    Robot World HTTP Client - Commands       ");
        System.out.println("    Format: [METHOD] [ACTION] [PARAMS]      ");
        System.out.println("                                             ");
        System.out.println("    GET world [name] - Get specific world    ");
        System.out.println("    GET world - Get current world            ");
        System.out.println("    POST launch [robot] - Create new robot   ");
        System.out.println("    POST [command] [robot] - Simple command  ");
        System.out.println("    POST [command] [robot] [arg] - Command with arg");
        System.out.println("                                             ");
        System.out.println("    Available commands:                     ");
        System.out.println("    look, forward, back, turn, reload, repair");
        System.out.println("---------------------------------------------");
    }

    private static void processUserInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            }

            List<String> commandParts = List.of(input.split(" "));
            try {
                executeCommand(commandParts);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Type commands in format: [METHOD] [ACTION] [PARAMS]");
            }
        }
        scanner.close();
        System.out.println("Client stopped");
    }

    private static void executeCommand(List<String> commandParts) {
        if (commandParts.isEmpty()) {
            throw new IllegalArgumentException("No command entered");
        }

        String method = commandParts.get(0).toUpperCase();
        String action = commandParts.size() > 1 ? commandParts.get(1).toLowerCase() : "";

        switch (method) {
            case "GET":
                handleGetCommand(action, commandParts);
                break;
            case "POST":
                handlePostCommand(action, commandParts);
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
    }

    private static void handleGetCommand(String action, List<String> commandParts) {
        if (!action.equals("world")) {
            throw new IllegalArgumentException("Invalid GET action. Only 'world' is supported");
        }

        if (commandParts.size() > 2) {
            getWorldRequest(commandParts.get(2));
        } else {
            getCurrentWorld();
        }
    }

    private static void handlePostCommand(String action, List<String> commandParts) {
        if (commandParts.size() < 3) {
            throw new IllegalArgumentException("Missing robot name");
        }

        String robotName = commandParts.get(2);
        switch (action) {
            case "launch":
                launchRobot(robotName);
                break;
            case "look":
            case "reload":
            case "repair":
                processRobotCommand(robotName, action);
                break;
            case "forward":
            case "back":
            case "turn":
                if (commandParts.size() < 4) {
                    throw new IllegalArgumentException("Missing argument for " + action);
                }
                processRobotCommandWithArgument(robotName, action, commandParts.get(3));
                break;
            default:
                throw new IllegalArgumentException("Unknown command: " + action);
        }
    }
}