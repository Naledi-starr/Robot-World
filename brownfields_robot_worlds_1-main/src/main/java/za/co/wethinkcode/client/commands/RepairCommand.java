package za.co.wethinkcode.client.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;

/**
 * Command to repair a robot's shields by entering a temporary REPAIR state.
 */

public class RepairCommand implements Command {
    private final World world;
    private final Robot robot;
    private int repairTime;

    /**
     * Constructs a RepairCommand for the given robot and world.
     *
     * @param world the game world
     * @param robot the robot performing the repair
     */

    public RepairCommand(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
        this.repairTime= world.getRepairTime();
    }

    /**
     * Executes the repair by setting the robot to REPAIR status and restoring shields after a delay.
     *
     * @return a JSON response indicating success or error
     */

    @Override
    public String execute() {
        // Check if robot is already repairing or in another special state
        if (robot.getStatus() != Robot.Status.NORMAL) {
            return createErrorResponse("Robot is currently busy and cannot repair");
        }

        // Set robot to repair state
        robot.setStatus(Robot.Status.REPAIR);

        // Get repair time from world config
        int repairTime = world.getRepairTime();

        // Create a new thread to handle the repair delay
        new Thread(() -> {
            try {
                // Wait for the repair time
                Thread.sleep(repairTime * 1000L);

                // After repair time is done, restore shields and set status back to normal
                synchronized (robot) {
                    robot.setShields(world.getMaxShieldStrength());
                    robot.setStatus(Robot.Status.NORMAL);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return createSuccessResponse(repairTime);
    }

    private String createSuccessResponse(int repairTime) {
        this.repairTime = repairTime;
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Done");
        response.add("data", data);

        response.add("state", new StateCommand(robot).toJson());
        return new Gson().toJson(response);
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "ERROR");
        response.addProperty("message", message);
        return new Gson().toJson(response);
    }

    /**
     * Returns the name of this command.
     *
     * @return "repair"
     */

    @Override
    public String getName() {
        return "repair";
    }

    /**
     * Returns a human-readable description of this command.
     *
     * @return a string describing the repair action
     */
    @Override
    public String display() {
        return "Instruct the robot to repair its shields";
    }
}
