package za.co.wethinkcode.server;

import za.co.wethinkcode.database.DbConfig;
import za.co.wethinkcode.flow.Recorder;
import za.co.wethinkcode.server.commands.SaveCommand;
import za.co.wethinkcode.server.handler.ClientHandler;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.Lake;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Pit;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class RobotWorldServer {
    private static int PORT = 5000;
    private static World world;

    public static void main(String[] args) {

        // initialize database
        DbConfig.main(new String[]{});

        try {
            new Recorder().logRun();
        } catch (Exception e) {
            System.err.println("Failed to record git: " + e.getMessage());
        }

        try {
            // Process command line arguments
            Map<String, String> arguments = parseArguments(args);

            // Handle port argument
            if (arguments.containsKey("-p")) {
                PORT = Integer.parseInt(arguments.get("-p"));
            }

            // Handle world size argument or load from config
            WorldConfig config;
            if (arguments.containsKey("-s")) {
                int worldSize = Integer.parseInt(arguments.get("-s"));
                Properties props = new Properties();
                props.setProperty("WORLD_WIDTH", String.valueOf(worldSize));
                props.setProperty("WORLD_HEIGHT", String.valueOf(worldSize));
                props.setProperty("NUM_PITS", "0");
                props.setProperty("NUM_MOUNTAINS", "0");
                props.setProperty("NUM_LAKES", "0");
                props.setProperty("VISIBILITY_RANGE", "5");
                props.setProperty("MAX_SHIELD_STRENGTH", "5");
                props.setProperty("REPAIR_TIME", "5");
                props.setProperty("RELOAD_TIME", "5");
                props.setProperty("MAX_SHOTS", "10");
                config = new WorldConfig(props);
            } else {
                config = new WorldConfig("config.properties");
            }

            world = new World(config);

            // Handle obstacle argument
            if (arguments.containsKey("-o")) {
                String[] coords = arguments.get("-o").split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);

                Random rand = new Random();
                int obstacleType = rand.nextInt(3);

                if (obstacleType == 0) {
                    Mountain mountain = new Mountain(x, y, 1, 1);
                    world.addObstacle(mountain);
                } else if (obstacleType == 1) {
                    Lake lake = new Lake(x, y, 1, 1);
                    world.addObstacle(lake);
                } else {
                    Pit pit = new Pit(x, y, 1, 1);
                    world.addObstacle(pit);
                }
            }

            // Rest of the original code remains unchanged
            Thread serverThread = new Thread(() -> {
                try {
                    startServer(world);
                } catch (IOException e) {
                    System.err.println("Server error: " + e.getMessage());
                }
            });
            serverThread.start();

            if (args.length == 0 || !args[0].equals("nogui")) {
                new ServerConsole(new Scanner(System.in), world).start();
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to parse command line arguments
    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-") && i + 1 < args.length && !args[i + 1].startsWith("-")) {
                arguments.put(args[i], args[i + 1]);
                i++; // Skip the next item as it's the value
            }
        }
        return arguments;
    }

    private static void startServer(World world) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Robot World Server running on port " + PORT);

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

    public static void setWorld(World newWorld) {
        world = newWorld;
    }

    public static World getWorld() {
        if (world == null) {
            System.out.println("RobotWorldServer: Initializing default world");
            WorldConfig config = new WorldConfig();
            world = new World(config);
        }
        return world;
    }
}