package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.client.commands.StateCommand;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;

import java.util.List;

/**
 * Command that displays all robots currently active in the world.
 * Used for debugging and monitoring robot statuses on the server side.
 */
public class RobotsCommand implements Command {
    private final World world;

    // ANSI styling
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    /**
     * Constructs a RobotsCommand with the given world.
     *
     * @param world the game world containing robot instances
     */
    public RobotsCommand(World world) {
        this.world = world;
    }

    /**
     * Executes the robots command.
     * Returns a static "OK" string since actual output is shown via {@code display()}.
     *
     * @return "OK" indicating successful execution
     */
    @Override
    public String execute() {
        return "OK";
    }


    /**
     * Provides a formatted string representing all robots in the world,
     * including their name, model, position, direction, shields, shots, and status.
     *
     * @return a human-readable list of robots and their states
     */
    @Override
    public String display() {
        List<Robot> robots = world.getRobots();

        if (robots.isEmpty()) {
            return YELLOW + "\n⚠ No robots currently in the world." + RESET + "\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(BOLD).append("\n🤖 ROBOTS IN WORLD").append(RESET).append("\n\n");
        sb.append(String.format("%-8s %-10s %-15s %-5s %-8s %-8s %-8s\n",
                "NAME", "MODEL", "POS", "DIR", "SHIELDS", "SHOTS", "STATUS"));
        sb.append("--------------------------------------------------------------------\n");

        for (Robot robot : robots) {
            sb.append(formatRobot(robot));
        }

        return sb.toString();
    }

    private String formatRobot(Robot robot) {
        StateCommand state = new StateCommand(robot);
        String color = robot.getStatus() == Robot.Status.DEAD ? "\u001B[31m" : "\u001B[32m";
        String reset = "\u001B[0m";

        return String.format("%s%-8s %-10s %-15s %-5s %-8d %-8d %-8s%s\n",
                color,
                robot.getName(),
                robot.getMake(),
                String.format("(%3d, %3d)", robot.getPosition().getX(), robot.getPosition().getY()),
                state.getDirectionShort(),
                robot.getShields(),
                robot.getShots(),
                robot.getStatus(),
                reset);
    }
    /**
     * Returns the name of this command.
     *
     * @return the command name "robots"
     */
    @Override
    public String getName() {
        return "robots";
    }
}
