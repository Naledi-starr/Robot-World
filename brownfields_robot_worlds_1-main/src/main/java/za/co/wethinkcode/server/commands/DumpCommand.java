package za.co.wethinkcode.server.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import za.co.wethinkcode.client.commands.StateCommand;
import za.co.wethinkcode.server.world.WorldDumper;
import za.co.wethinkcode.server.world.obstacles.Obstacle;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;

import java.util.List;

/**
 * Command to generate a detailed dump of the current world state,
 * including obstacles and robots, returned as JSON or formatted text.
 */

public class DumpCommand implements Command {
    private final World world;
    private final WorldDumper worldDumper;

    // ANSI styling
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BOLD = "\u001B[1m";

    /**
     * Constructs a DumpCommand for the given world.
     *
     * @param world the game world to dump
     */
    public DumpCommand(World world) {
        this.world = world;
        this.worldDumper = new WorldDumper(world);
    }

    /**
     * Executes the dump command, returning the world state as a JSON string.
     *
     * @return JSON string containing the world dump data
     */
    @Override
    public String execute() {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        response.add("data", worldDumper.createWorldDump());
        return new Gson().toJson(response);
    }

    /**
     * Returns a human-readable textual representation of the world dump,
     * including obstacle and robot details.
     *
     * @return formatted string with world details for display
     */
    @Override
    public String display() {
        StringBuilder sb = new StringBuilder();

        // World Header
        sb.append(BOLD).append("\n🌍 WORLD DUMP").append(RESET).append("\n");
        sb.append("  Size: ")
                .append(world.getWidth())
                .append(" x ")
                .append(world.getHeight())
                .append("\n");

        // Obstacles Section
        sb.append("\n").append(BOLD).append("🧱 Obstacles").append(RESET).append("\n");
        Obstacle[] obstacles = world.getObstacles().toArray(new Obstacle[0]);
        if (obstacles.length == 0) {
            sb.append(YELLOW).append("  No obstacles in world").append(RESET).append("\n");
        } else {
            sb.append(String.format("%-15s %-12s %-8s\n", "TYPE", "POSITION", "SIZE"));
            sb.append("----------------------------------------\n");
            for (Obstacle obstacle : obstacles) {
                sb.append(String.format("%-15s (%3d,%3d)    %d x %d\n",
                        obstacle.getType(),
                        obstacle.getX(), obstacle.getY(),
                        obstacle.getWidth(), obstacle.getHeight()));
            }
        }

        // Robots Section
        sb.append("\n").append(BOLD).append("🤖 Robots").append(RESET).append("\n");
        List<Robot> robots = world.getRobots();
        if (robots.isEmpty()) {
            sb.append(YELLOW).append("  No robots in world").append(RESET).append("\n");
        } else {
            sb.append(String.format("%-8s %-10s %-13s %-5s %-8s %-8s %-8s\n",
                    "NAME", "MODEL", "POSITION", "DIR", "SHIELDS", "SHOTS", "STATUS"));
            sb.append("-------------------------------------------------------------------\n");
            for (Robot robot : robots) {
                sb.append(formatRobot(robot));
            }
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
     * @return the command name "dump"
     */
    @Override
    public String getName() {
        return "dump";
    }
}
