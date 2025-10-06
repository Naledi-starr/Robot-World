package za.co.wethinkcode.client;

import za.co.wethinkcode.server.ServerConsole;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.handler.ClientHandler;
import za.co.wethinkcode.server.world.World;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Constructs a {@code MainMenu} using the provided {@link Scanner}.
 * Loads world configuration from "config.properties" if available; otherwise, uses default settings.
 *
 * @param {@code Scanner} to capture user input from the console
 */

public class MainMenu {
    private final Scanner scanner;
    private final World world;


    public MainMenu(Scanner scanner) {
        this.scanner = scanner;
        World cofiguredWorld;
        // Initialize the world with default parameters
        try {
            WorldConfig config = new WorldConfig("config.properties");
            cofiguredWorld = new World(config);
        } catch (IOException e) {
            System.err.println("Failed to load config. Using default world settings.");
            WorldConfig defaultConfig = new WorldConfig(); // to use defaults
            cofiguredWorld = new World(defaultConfig);
        }
        this.world = cofiguredWorld;
    }

    /**
     * Displays the main menu options and prompts the user for a choice to run
     * the client, server, both, or exit the application.
     */

    public void start() {
        System.out.println("Robot World Simulation");
        System.out.println("======================");
        System.out.println("1. Start Client");
        System.out.println("2. Start Server");
        System.out.println("3. Start Both");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");

        int choice = getMenuChoice();
        handleChoice(choice);
    }

    private int getMenuChoice() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter 1-4: ");
            }
        }
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1:
                new Play(scanner).start();
                break;
            case 2:
                startServer();
                break;
            case 3:
                startBoth();
                break;
            case 4:
                System.out.println("Exiting...");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                start();
        }
    }


    private void startServer() {
        System.out.println("Starting server...");
        // Start the server with our world instance
        new Thread(() -> {
            try {
                startServerThread(world);
            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
            }
        }).start();

        // Start the console with the same world instance
        new ServerConsole(scanner, world).start();
    }

    private void startBoth() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      HOW TO RUN SERVER AND CLIENT IN INTELLIJ    ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  Follow these steps to run both applications:    ║");
        System.out.println("║                                                  ║");
        System.out.println("║  1. SET UP RUN CONFIGURATIONS:                   ║");
        System.out.println("║     • Go to Run > Edit Configurations...         ║");
        System.out.println("║     • Click '+' and add two Application configs  ║");
        System.out.println("║                                                  ║");
        System.out.println("║  2. SERVER CONFIGURATION:                        ║");
        System.out.println("║     • Name: Robot Server                         ║");
        System.out.println("║     • Main class: za.co.wethinkcode.client.MainMenu");
        System.out.println("║     • Program arguments: server                  ║");
        System.out.println("║     • ✔️ Check 'Allow multiple instances'        ║");
        System.out.println("║                                                  ║");
        System.out.println("║  3. CLIENT CONFIGURATION:                        ║");
        System.out.println("║     • Name: Robot Client                         ║");
        System.out.println("║     • Main class: za.co.wethinkcode.client.Play  ║");
        System.out.println("║     • Program arguments: (leave empty)           ║");
        System.out.println("║     • ✔️ Check 'Allow multiple instances'        ║");
        System.out.println("║                                                  ║");
        System.out.println("║  4. RUNNING:                                     ║");
        System.out.println("║     • First run 'Robot Server' (let it start)    ║");
        System.out.println("║     • Then run 'Robot Client' in a separate run  ║");
        System.out.println("║     • Each will open in its own Run tab          ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        System.out.println("\nPress Enter to return to menu...");
        scanner.nextLine();
    }

    private static void startServerThread(World world) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Robot World Server running on port 5000");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    new ClientHandler(clientSocket, world).start();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Entry point of the program. Depending on the arguments passed,
     * either starts the server in server-only mode or displays the main menu.
     */

    public static void main(String[] args) {
        try {
            WorldConfig config = new WorldConfig("config.properties");
            World world = new World(config);

            if (args.length > 0 && args[0].equals("server")) {
                new Thread(() -> {
                    try {
                        startServerThread(world);
                    } catch (IOException e) {
                        System.err.println("Server error: " + e.getMessage());
                    }
                }).start();
                new ServerConsole(new Scanner(System.in), world).start();
            } else {
                new MainMenu(new Scanner(System.in)).start();
            }
        } catch (IOException e) {
            System.err.println("Failed to load config in main. Exiting.");
            e.printStackTrace();
        }
    }
}
