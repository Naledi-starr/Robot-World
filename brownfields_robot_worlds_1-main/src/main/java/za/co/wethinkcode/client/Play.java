package za.co.wethinkcode.client;

import com.google.gson.*;
import za.co.wethinkcode.protocol.client.CommandProcessor;
import za.co.wethinkcode.client.connection.ClientConnection;
import za.co.wethinkcode.client.robots.Robot;
import za.co.wethinkcode.client.robots.RobotMaker;

import java.util.Scanner;

/**
 * Handles the main client-side game logic for the Robot Simulation Console.
 * Responsible for connecting to the server, robot setup, processing user input,
 * and managing the game session lifecycle.
 */

public class Play {
    private final Scanner scanner;
    private final RobotMaker robotMaker;
    private final ClientConnection clientConnection;
    private final CommandProcessor commandProcessor;
    private String currentRobotName;

    // ANSI escape codes for terminal color output
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\033[1m";
    private static final String GRAY = "\u001B[90m";

    /**
     * Constructs a new Play instance with the specified Scanner for user input.
     * Initializes robot creation, command processing, and server connection components.
     *
     * @param scanner Scanner to read input from the console
     */

    public Play(Scanner scanner) {
        this.scanner = scanner;
        this.robotMaker = new RobotMaker();
        this.clientConnection = new ClientConnection();
        this.commandProcessor = new CommandProcessor(this);
    }

    /**
     * Starts the simulation session by connecting to the server, deploying the robot,
     * and entering the main command loop.
     */

    public void start() {
        welcomeMessage();
        connectToServer();

        if (clientConnection.isConnected()) {
            deployRobot();
            runGameLoop();
        }

        shutdown();
    }

    private void welcomeMessage() {
        System.out.println(BOLD + "\n════════════════════════════════════════════════════");
        System.out.println("      🚀 Welcome to the Robot Simulation Console");
        System.out.println("════════════════════════════════════════════════════" + RESET);
    }


    private void connectToServer() {
        System.out.println(BOLD + "\n🔌 Connect to Server" + RESET);
        String ip = prompt(" IP [localhost]: ", "localhost");
        int port = promptPort(" Port [5000]: ", 5000);

        if (clientConnection.connect(ip, port)) {
            System.out.println(GREEN + " ✅ Connected successfully!" + RESET);
        } else {
            System.out.println(RED + " ❌ Failed to connect." + RESET);
        }
    }

    private String prompt(String label, String defaultVal) {
        System.out.print(label);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultVal : input;
    }

    private int promptPort(String label, int defaultPort) {
        while (true) {
            System.out.print(label);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return defaultPort;
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(RED + " Invalid port. Try again." + RESET);
            }
        }
    }

    private void deployRobot() {
        System.out.println(BOLD + "\n🤖 Deploy Your Robot" + RESET);
        Robot robot = createRobot();
        currentRobotName = robot.getName();
        commandProcessor.setRobotDetails(robot.getMake(), robot.getName());

        System.out.println(GREEN + " ✅ Robot '" + robot.getName() + "' is ready." + RESET);
        printCommandList();
    }

    private Robot createRobot() {
        String name = promptRobotName();
        String make = promptRobotMake();
        return robotMaker.createRobot(name, make);
    }

    private String promptRobotName() {
        while (true) {
            System.out.print(" Robot name (no spaces): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println(RED + " Name can't be empty." + RESET);
            } else if (name.contains(" ")) {
                System.out.println(RED + " Name must not contain spaces." + RESET);
            } else {
                return name;
            }
        }
    }

    private String promptRobotMake() {
        String[] makes = robotMaker.getStandardMakes();
        System.out.println(" Available Models:");
        for (int i = 0; i < makes.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, makes[i]);
        }

        while (true) {
            System.out.print(" Select model [1-" + makes.length + "]: ");
            String input = scanner.nextLine().trim();
            try {
                int choice = input.isEmpty() ? 1 : Integer.parseInt(input);
                if (choice >= 1 && choice <= makes.length) {
                    return makes[choice - 1];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println(RED + " Invalid choice. Try again." + RESET);
        }
    }

    private void printCommandList() {
        System.out.println(BOLD + "\n📜 Available Commands:" + RESET);
        System.out.println(GRAY + " ─────────────────────────────" + RESET);
        System.out.println("  launch [make] [name]");
        System.out.println("  look [robot]");
        System.out.println("  state [robot]");
        System.out.println("  forward [robot] [steps]");
        System.out.println("  back [robot] [steps]");
        System.out.println("  turn [robot] [left|right]");
        System.out.println("  fire [robot]");
        System.out.println("  reload [robot]");
        System.out.println("  repair [robot]");
        System.out.println("  exit");
        System.out.println(GRAY + " ─────────────────────────────" + RESET);
    }

    private void runGameLoop() {
        while (true) {
            System.out.print("\n" + BOLD + "Enter Command: " + RESET);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println(YELLOW + "\n👋 Exiting Robot Simulation. Goodbye!" + RESET);
                break;
            }

            processCommand(input);
        }
    }

    private void processCommand(String input) {
        String json = commandProcessor.convertToJsonCommand(input);

        if (json == null || json.contains("\"result\":\"ERROR\"")) {
            System.out.println(RED + " Invalid command format." + RESET);
            return;
        }

        clientConnection.send(json);
        String response = clientConnection.receive();

        if (response != null) {
            displayJsonResponse(response);
        } else {
            System.out.println(RED + " No response received." + RESET);
        }
    }


    private void displayJsonResponse(String jsonResponse) {
        try {
            JsonObject response = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String result = response.get("result").getAsString();
            String resultColor = result.equalsIgnoreCase("OK") ? GREEN : RED;

            System.out.println(resultColor + " ▶ Result: " + result + RESET);

            if (response.has("data")) {
                JsonObject data = response.getAsJsonObject("data");
                System.out.println(BOLD + "\n📦 Data:" + RESET);

                // Handle regular data fields
                for (String key : data.keySet()) {
                    if (!key.equals("state")) {
                        System.out.printf("  %s%s:%s %s%s%s%n",
                                WHITE, key, RESET,
                                CYAN, data.get(key), RESET);
                    }
                }

                // Special handling for state in data (hit robot's state)
                if (data.has("state")) {
                    JsonObject state = data.getAsJsonObject("state");
                    System.out.println(YELLOW + "\n💥 Hit Robot State:" + RESET);
                    prettyPrintState(state);
                }

                if (data.has("objects")) {
                    JsonArray objects = data.getAsJsonArray("objects");
                    System.out.println(YELLOW + "\n🧱 Nearby Objects:" + RESET);
                    for (JsonElement obj : objects) {
                        JsonObject o = obj.getAsJsonObject();
                        System.out.printf("  → %s%s%s | %s%s%s | %s%d%s%n",
                                CYAN, o.get("direction").getAsString(), RESET,
                                CYAN, o.get("type").getAsString(), RESET,
                                CYAN, o.get("distance").getAsInt(), RESET);
                    }
                }
            }

            if (response.has("state")) {
                JsonObject state = response.getAsJsonObject("state");
                System.out.println(BOLD + "\n🧠 Robot State:" + RESET);
                prettyPrintState(state);
            }
        } catch (Exception e) {
            System.out.println(RED + " ⚠ Could not parse response:" + RESET);
            System.out.println(jsonResponse);
        }
    }

    private void prettyPrintState(JsonObject state) {
        if (state.has("position")) {
            JsonArray pos = state.getAsJsonArray("position");
            System.out.printf("  %sPosition:%s (%s%d%s, %s%d%s)%n",
                    WHITE, RESET,
                    CYAN, pos.get(0).getAsInt(), RESET,
                    CYAN, pos.get(1).getAsInt(), RESET);
        }
        if (state.has("direction")) {
            System.out.printf("  %sFacing:%s %s%s%s%n",
                    WHITE, RESET,
                    CYAN, state.get("direction").getAsString(), RESET);
        }
        if (state.has("shields")) {
            System.out.printf("  %sShields:%s %s%d%s%n",
                    WHITE, RESET,
                    CYAN, state.get("shields").getAsInt(), RESET);
        }
        if (state.has("shots")) {
            System.out.printf("  %sShots:%s %s%d%s%n",
                    WHITE, RESET,
                    CYAN, state.get("shots").getAsInt(), RESET);
        }
        if (state.has("status")) {
            System.out.printf("  %sStatus:%s %s%s%s%n",
                    WHITE, RESET,
                    CYAN, state.get("status").getAsString(), RESET);
        }
    }

    private void shutdown() {
        clientConnection.disconnect();
        scanner.close();
    }
    /**
     * Retrieves the name of the currently active robot in the game session.
     *
     * @return the current robot's name
     */

    public String getCurrentRobotName() {
        return currentRobotName;
    }

    /**
     * Launches the application and starts a new game session.
     *
     * @param args command-line arguments (not used)
     */

    public static void main(String[] args) {
        new Play(new Scanner(System.in)).start();
    }
}
