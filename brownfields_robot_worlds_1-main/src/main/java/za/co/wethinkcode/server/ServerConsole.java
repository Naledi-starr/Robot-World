package za.co.wethinkcode.server;

import za.co.wethinkcode.protocol.server.CommandFactory;
import za.co.wethinkcode.server.entities.WorldProperty;
import za.co.wethinkcode.server.persistence.WorldDAOInterface;
import za.co.wethinkcode.server.persistence.WorldDao;
import za.co.wethinkcode.server.world.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * ServerConsole provides an interactive command-line interface for server operators.
 * It allows inspection of the world state, robot management, and graceful shutdown.
 */
public class ServerConsole {
    private final Scanner scanner;
    private CommandFactory commandFactory; // Non-final to allow reassignment
    private boolean isRunning;
    private final WorldDao worldDao;
    WorldDAOInterface dao;

    // ANSI formatting
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BOLD = "\u001B[1m";

    /**
     * Constructs a new ServerConsole instance.
     *
     * @param scanner the Scanner used for reading input from the command line
     * @param world   the World object representing the server's game world
     */
    public ServerConsole(Scanner scanner, World world) {
        this.scanner = scanner;
        this.commandFactory = new CommandFactory(world);
        this.isRunning = true;
        this.worldDao = new WorldDao("jdbc:sqlite:robot-worlds.db");
    }

    /**
     * Starts the console interface, allowing users to enter commands such as
     * 'help', 'robots', 'dump', 'quit', and 'restore'.
     */
    public void start() {
        System.out.println(BOLD + "\n═══════════════════════════════════════════════");
        System.out.println("   🛠️  Robot World Server Console Started");
        System.out.println("═══════════════════════════════════════════════" + RESET);
        System.out.println(GREEN + " ✅ Server is up and running." + RESET);
        System.out.println(WHITE + " Type 'help' to see available commands." + RESET);

        while (isRunning) {
            System.out.print("\n" + BOLD + "Server> " + RESET);
            String input = scanner.nextLine().trim();
            processCommand(input);
        }
    }

    private void processCommand(String command) {
        switch (command.toLowerCase()) {
            case "quit":
                handleQuitCommand();
                break;
            case "save":
                try {
                    System.out.println(commandFactory.createSaveCommand().execute());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "help":
                showHelp();
                break;
            case "dump":
                System.out.println(commandFactory.createDumpCommand().display());
                break;
            case "robots":
                System.out.println(commandFactory.createRobotsCommand().display());
                break;
            case "restore":
                handleRestoreCommand();
                break;
            case "worlds":
                try {
                    String sql = "SELECT w.id, w.world_name, w.world_size, w.created_at, " +
                            "o.obstacle_name, wp.x_cord, wp.y_cord, wp.obstacle_width, wp.obstacle_height " +
                            "FROM saved_worlds w " +
                            "LEFT JOIN world_properties wp ON w.id = wp.world_id " +
                            "LEFT JOIN obstacles o ON wp.obstacle_id = o.id " +
                            "ORDER BY w.id";

                    try (PreparedStatement pstmt = worldDao.getConnection().prepareStatement(sql);
                         ResultSet rs = pstmt.executeQuery()) {

                        int lastWorldId = -1;
                        while (rs.next()) {
                            String worldName = rs.getString("world_name");
                            int worldSize = rs.getInt("world_size");

                            Timestamp ts = rs.getTimestamp("created_at");
                            String createdAtStr;

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                            if (ts != null) {
                                createdAtStr = ts.toLocalDateTime().format(formatter); // convert to LocalDateTime
                            } else {
                                createdAtStr = "> 3 weeks ago";
                            }
                            int worldId = rs.getInt("id");
                            if (worldId != lastWorldId) {
                                System.out.printf("- 🌍 %s (size: %d, created: %s)%n",
                                        worldName, worldSize, createdAtStr);
                                lastWorldId = worldId;
                            }

                            String obstacle = rs.getString("obstacle_name");
                            if (obstacle != null) {
                                System.out.printf("    Obstacle: %s at (%d,%d) size [%d x %d]%n",
                                        obstacle,
                                        rs.getInt("x_cord"),
                                        rs.getInt("y_cord"),
                                        rs.getInt("obstacle_width"),
                                        rs.getInt("obstacle_height"));
                            }
                        }
                    }

                } catch (SQLException e) {
                    System.out.println("Error fetching worlds: " + e.getMessage());
                }
                break;


            default:
                System.out.println(RED + " ❌ Unknown command. Type 'help' for available commands." + RESET);
        }
    }

    private void handleQuitCommand() {
        System.out.println(YELLOW + "\n👋 Disconnecting all robots and shutting down..." + RESET);
        try {
            commandFactory.createQuitCommand().execute();
            isRunning = false;
        } catch (SQLException e) {
            System.out.println(RED + " ❌ Failed to quit: " + e.getMessage() + RESET);
        }
    }

    public void handleRestoreCommand() {
        try {
            World restoredWorld = worldDao.restoreWorld();
            RobotWorldServer.setWorld(restoredWorld);
            this.commandFactory = new CommandFactory(RobotWorldServer.getWorld());
            System.out.println(GREEN + " ✅ World restored successfully." + RESET);
        } catch (SQLException e) {
            System.out.println(RED + " ❌ Failed to restore world: " + e.getMessage() + RESET);
        }
    }

    private void showHelp() {
        System.out.println(BOLD + "\n📚 Server Commands:" + RESET);
        System.out.println(WHITE + "────────────────────────────────" + RESET);
        System.out.println(" dump    - Show current world state");
        System.out.println(" save    - Save current world to database");
        System.out.println(" robots  - List all active robots");
        System.out.println(" restore - Restore world from database");
        System.out.println(" worlds - Display all saved worlds from database");
        System.out.println(" quit    - Disconnect all and shut down");
        System.out.println(" help    - Show this help message");
    }
}