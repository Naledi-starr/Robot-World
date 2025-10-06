package za.co.wethinkcode.client.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.ObjectInDirection;
import za.co.wethinkcode.server.world.World;

/**
 * Represents a visual scan command for a robot.
 * Scans the world for visible objects and builds a JSON response.
 */
public class LookCommand {
    private final JsonArray objects = new JsonArray();
    private final World world;
    private final Robot robot;
    private final int visibilityRange;
    private final Gson gson = new Gson();

    /**
     * Constructs a {@code LookCommand} for the specified robot and world.
     *
     * @param world the game world
     * @param robot the robot performing the look
     */
    public LookCommand(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
        this.visibilityRange = world.getVisibilityRange();
    }

    /**
     * Executes the look command, scanning all directions for obstacles or robots.
     *
     * @return JSON string with detected objects and robot state
     */
    public String execute() {
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (Direction dir : directions) {
            ObjectInDirection obj = world.findClosestObjectInDirection(
                    robot.getPosition(), dir, visibilityRange, robot
            );
            if (obj != null) {
                // Ensure type is uppercase to match expected output
                String type = obj.type.toUpperCase();
                addObject(dir.toString(), type, obj.distance);
            }
        }

        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        JsonObject data = new JsonObject();
        data.add("objects", objects);
        data.addProperty("visibilityRange", visibilityRange);
        response.add("data", data);
        response.add("state", new StateCommand(robot).toJson());
        return gson.toJson(response);
    }

    /**
     * Adds a visible object to the scan result.
     *
     * @param direction the direction where the object is seen
     * @param type      the type of object (e.g., "ROBOT", "PIT")
     * @param distance  how far the object is from the robot
     */
    public void addObject(String direction, String type, int distance) {
        JsonObject obj = new JsonObject();
        obj.addProperty("direction", direction.toUpperCase()); // Ensure direction is uppercase
        obj.addProperty("type", type);
        obj.addProperty("distance", distance);
        objects.add(obj);
    }

    /**
     * Returns the scan result as a JSON object.
     *
     * @return a JSON representation of what the robot sees
     */
    public JsonObject toJson() {
        JsonObject visionJson = new JsonObject();
        visionJson.add("objects", objects);
        visionJson.addProperty("visibilityRange", visibilityRange);
        return visionJson;
    }
}