package za.co.wethinkcode.server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides vision functionality for a robot within the world.
 * Scans in all cardinal directions to detect obstacles, other robots, or world edges
 * within the robot's visibility range.
 */
public class VisionFinder {
    private final World world;
    private final Robot robot;

    public VisionFinder(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
    }

    public JsonObject findInAbsoluteDirections(Robot robot) {
        JsonObject visionData = new JsonObject();
        JsonArray objects = new JsonArray();
        Set<String> reportedDirections = new HashSet<>();

        Position robotPos = robot.getPosition();

        for (Direction direction : Direction.values()) {
            if (!reportedDirections.contains(direction.name())) {
                scanDirection(objects, reportedDirections, robotPos, direction);
            }
        }

        checkImmediateEdges(objects, reportedDirections, robotPos);

        visionData.add("objects", objects);
        visionData.addProperty("visibilityRange", world.getVisibilityRange());
        return visionData;
    }

    private void scanDirection(JsonArray objects, Set<String> reportedDirections,
                               Position start, Direction direction) {
        Position current = start;

        for (int distance = 1; distance <= world.getVisibilityRange(); distance++) {
            current = getNextPosition(current, direction);

            if (!world.isPositionValid(current)) {
                report(objects, reportedDirections, direction.name(), "EDGE", distance);
                break;
            }

            int x = current.getX();
            int y = current.getY();

            Obstacle blockingObstacle = world.getObstacles().stream()
                    .filter(o -> o.blocksPosition(x, y))
                    .findFirst()
                    .orElse(null);

            if (blockingObstacle != null) {
                report(objects, reportedDirections, direction.name(), blockingObstacle.getType().toUpperCase(), distance);
                break;
            }

            // Create a new variable to hold the current position for the lambda
            Position finalCurrent = current;
            Robot blockingRobot = world.getRobots().stream()
                    .filter(r -> !r.equals(robot) && r.getPosition().equals(finalCurrent))
                    .findFirst()
                    .orElse(null);

            if (blockingRobot != null) {
                report(objects, reportedDirections, direction.name(), "ROBOT", distance);
                break;
            }
        }
    }


    private void checkImmediateEdges(JsonArray objects, Set<String> reportedDirections, Position pos) {
        if (pos.getY() == 0 && !reportedDirections.contains("NORTH")) {
            report(objects, reportedDirections, "NORTH", "EDGE", 1);
        }
        if (pos.getY() == world.getHeight() - 1 && !reportedDirections.contains("SOUTH")) {
            report(objects, reportedDirections, "SOUTH", "EDGE", 1);
        }
        if (pos.getX() == world.getWidth() - 1 && !reportedDirections.contains("EAST")) {
            report(objects, reportedDirections, "EAST", "EDGE", 1);
        }
        if (pos.getX() == 0 && !reportedDirections.contains("WEST")) {
            report(objects, reportedDirections, "WEST", "EDGE", 1);
        }
    }

    private Position getNextPosition(Position current, Direction direction) {
        return switch (direction) {
            case NORTH -> new Position(current.getX(), current.getY() - 1);
            case EAST  -> new Position(current.getX() + 1, current.getY());
            case SOUTH -> new Position(current.getX(), current.getY() + 1);
            case WEST  -> new Position(current.getX() - 1, current.getY());
        };
    }

    private void report(JsonArray objects, Set<String> reportedDirections, String direction, String type, int distance) {
        JsonObject obj = new JsonObject();
        obj.addProperty("direction", direction);
        obj.addProperty("type", type);
        obj.addProperty("distance", distance);
        objects.add(obj);
        reportedDirections.add(direction);
    }
}
