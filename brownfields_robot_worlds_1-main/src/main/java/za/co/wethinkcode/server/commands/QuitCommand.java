package za.co.wethinkcode.server.commands;
import za.co.wethinkcode.server.world.World;

/**
 * Command to shut down the server by disconnecting all robots
 * and terminating the application.
 * Intended for server console use only.
 */
public class QuitCommand implements Command {
    private final World world;

    /**
     * Constructs a QuitCommand with the given world.
     *
     * @param world the game world containing robots to disconnect
     */
    public QuitCommand(World world) {
        this.world = world;
    }

    /**
     * Executes the quit command by disconnecting all robots with
     * a brief delay for visibility, then shutting down the server.
     *
     * @return a message indicating the shutdown sequence
     */
    @Override
    public String execute() {
        StringBuilder response = new StringBuilder("Disconnecting all robots:\n");

        // Disconnect robots one by one with a brief pause
        world.getRobots().forEach(robot -> {
            String robotName = robot.getName();
            response.append("- ").append(robotName).append("\n");

            // Display the robot being disconnected
            System.out.println("Disconnecting robot: " + robotName);

            try {
                // Add a small delay to make the disconnection visible
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        response.append("Server shutting down...");
        System.out.println("Server shutting down...");

        // Give a small delay before actual shutdown
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.exit(0);
        return response.toString();
    }

    /**
     * Returns the name of the command.
     *
     * @return the command name "quit"
     */
    @Override
    public String getName() {
        return "quit";
    }

    /**
     * Returns a short description of the command.
     *
     * @return the command usage string
     */
    @Override
    public String display() {
        return "quit - Shut down the server and disconnect all robots";
    }
}