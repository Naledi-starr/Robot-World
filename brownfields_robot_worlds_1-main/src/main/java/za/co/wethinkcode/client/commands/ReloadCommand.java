package za.co.wethinkcode.client.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.client.commands.StateCommand;

/**
 * ReloadCommand allows a robot to reload its weapon.
 *
 * When executed, the robot enters a RELOAD status for a configured period,
 * during which it cannot perform any other actions. After the reload delay,
 * the robot's shots are refilled, and status returns to NORMAL.
 */

public class ReloadCommand implements Command {
    private final World world;
    private final Robot robot;
    private volatile boolean isReloading = false;

    /**
     * Constructs a ReloadCommand for a robot within a world.
     *
     * @param = world the game world containing the robot
     * @param = robot the robot that will reload
     */

    public ReloadCommand(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
    }

    /**
     * Executes the reload command.
     *
     * If the robot is not currently busy, it starts reloading by setting the status to RELOAD
     * and spawning a thread that waits for the reload time. After the delay, the robot's shots
     * are refilled and status is reset to NORMAL. If already busy, returns an error response.
     *
     * @return JSON string indicating success or error
     */

    @Override
    public synchronized String execute() {
        // Check if robot is already reloading or in another special state
        if (robot.getStatus() != Robot.Status.NORMAL || isReloading) {
            return createErrorResponse("Robot is currently busy and cannot reload");
        }

        // Mark as reloading
        isReloading = true;
        robot.setStatus(Robot.Status.RELOAD);

        // Get reload time from world config
        int reloadTime = world.getReloadTime();

        // Create a new thread to handle the reload delay
        new Thread(() -> {
            try {
                // Wait for the reload time
                Thread.sleep(reloadTime * 1000L);

                // After reload time is done, reset shots to max and set status back to normal
                synchronized (this) {
                    robot.setShots(world.getMaxShots());
                    robot.setStatus(Robot.Status.NORMAL);
                    isReloading = false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                synchronized (this) {
                    isReloading = false;
                }
            }
        }).start();

        return createSuccessResponse(reloadTime);
    }

    /**
     * Creates a success JSON response indicating the reload has started.
     *
     * @param = reloadTime duration in seconds for the reload
     * @return JSON string with result OK and reload time
     */

    public String createSuccessResponse(int reloadTime) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Reloading weapons");
        data.addProperty("reloadTime", reloadTime);
        response.add("data", data);

        response.add("state", new StateCommand(robot).toJson());
        return new Gson().toJson(response);
    }

    /**
     * Creates an error JSON response if reload cannot proceed.
     *
     * @param = message the error message to include
     * @return JSON string with result ERROR and the message
     */

    public String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "ERROR");
        response.addProperty("message", message);
        return new Gson().toJson(response);
    }

    /**
     * Returns the command name.
     *
     * @return "reload"
     */

    @Override
    public String getName() {
        return "reload";
    }

    /**
     * Returns a user-friendly description of the command.
     *
     * @return description string
     */

    @Override
    public String display() {
        return "Reload weapons";
    }
}