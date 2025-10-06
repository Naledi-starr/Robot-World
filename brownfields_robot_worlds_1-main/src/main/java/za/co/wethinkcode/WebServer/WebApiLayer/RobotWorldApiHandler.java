package za.co.wethinkcode.WebServer.WebApiLayer;

import com.google.gson.reflect.TypeToken;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import za.co.wethinkcode.server.RobotWorldServer;
import za.co.wethinkcode.server.persistence.WorldDao;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Position;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles HTTP requests for the RobotWorld Web API, processing GET and POST
 * endpoints to retrieve world data and manage robot commands. Integrates with
 * the Domain layer (World, Robot) and Data Access layer (WorldDao) while
 * maintaining separation of concerns.
 */
public class RobotWorldApiHandler {
    private static final WorldDao worldDao = new WorldDao("jdbc:sqlite:robot-worlds.db");

    /**
     * Handles GET /world and GET /world/{name} requests, returning the current
     * or specified world's objects (obstacles, robots) as JSON. Restores a
     * specific world from the database if a name is provided.
     *
     * @param context the Javalin HTTP context containing request and response data
     * @throws NotFoundResponse if the specified world cannot be restored due to a database error
     */
    public static void getWorld(Context context) {
        String worldName = context.pathParamAsClass("name", String.class)
                .getOrDefault(null);
        World world;
        try {
            if (worldName != null) {
                // Restore a specific world from the database
                world = worldDao.restoreWorld();
                RobotWorldServer.setWorld(world);
            } else {
                // Get current world
                world = RobotWorldServer.getWorld();
            }

            // Build JSON response with obstacles and robots
            Map<String, Object> response = new HashMap<>();
            List<Map<String, Object>> obstacles = new ArrayList<>();
            for (Obstacle obstacle : world.getObstacles()) {
                Map<String, Object> obs = new HashMap<>();
                obs.put("type", obstacle.getType());
                obs.put("x", obstacle.getX());
                obs.put("y", obstacle.getY());
                obs.put("width", obstacle.getWidth());
                obs.put("height", obstacle.getHeight());
                obstacles.add(obs);
            }
            response.put("obstacles", obstacles);
            response.put("robots", world.getRobots());
            response.put("width", world.getWidth());
            response.put("height", world.getHeight());

            context.json(response);
            context.status(200);
        } catch (SQLException e) {
            throw new NotFoundResponse("World not found: " + worldName + ", error: " + e.getMessage());
        }
    }

    /**
     * Handles POST /robot/{name} requests to process robot commands, currently
     * supporting only the "launch" command. Creates a new robot with the specified
     * name and initial position, sets its properties, and adds it to the world.
     * Returns a JSON response matching the socket server's format.
     *
     * @param context the Javalin HTTP context containing request and response data
     */
    public static void handleCommand(Context context) {
        String robotName = context.pathParamAsClass("name", String.class)
                .getOrDefault(null);
        try {
            // Parse JSON command from the request body
            Map<String, Object> requestBody = context.bodyAsClass(new TypeToken<Map<String, Object>>(){}.getType());            String command = (String) requestBody.get("command");
            @SuppressWarnings("unchecked")
            List<String> arguments = (List<String>) requestBody.get("arguments");

            if (!"launch".equalsIgnoreCase(command)) {
                context.status(400).json(Map.of("result", "ERROR", "message", "Only launch command is supported"));
                return;
            }

            // Validate arguments: [make, maxShields, maxShots]
            if (arguments.size() != 3) {
                context.status(400).json(Map.of("result", "ERROR", "message", "Launch command requires [make, maxShields, maxShots]"));
                return;
            }

            // Create robot with initial position (0,0)
            Robot robot = new Robot(robotName, new Position(0, 0));
            robot.setMake(arguments.get(0));
            robot.setShields(Integer.parseInt(arguments.get(1)));
            robot.setShots(Integer.parseInt(arguments.get(2)));

            // Add robot to a world
            World world = RobotWorldServer.getWorld();
            world.addRobot(robot);

            // Build response matching socket server format
            Map<String, Object> response = new HashMap<>();
            response.put("result", "OK");
            response.put("data", Map.of("message", "Robot launched successfully"));
            response.put("state", Map.of(
                    "position", List.of(robot.getPosition().getX(), robot.getPosition().getY()),
                    "direction", robot.getDirection().toString(),
                    "shields", robot.getShields(),
                    "shots", robot.getShots(),
                    "status", robot.getStatus().toString()
            ));

            context.json(response);
            context.status(201);
            context.header("Location", "/robot/" + robotName);
        } catch (Exception e) {
            context.status(400).json(Map.of("result", "ERROR", "message", "Invalid launch command: " + e.getMessage()));
        }
    }
}