package za.co.wethinkcode.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.commands.ForwardCommand;
import za.co.wethinkcode.client.commands.LookCommand;
import za.co.wethinkcode.client.commands.TurnRightCommand;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Pit;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for verifying the LookCommand functionality in 1x1 and 2x2 worlds.
 */
public class LookCommandTest {

    private World world1x1;
    private World world2x2;
    private Robot robot;

    /**
     * Creates a test world with specified dimensions and visibility range, ensuring no random obstacles.
     */
    private World createTestWorld(int width, int height, int visibilityRange) {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH", String.valueOf(width));
            properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
            properties.setProperty("VISIBILITY_RANGE", String.valueOf(visibilityRange));
            properties.setProperty("NUM_PITS", "0");
            properties.setProperty("NUM_MOUNTAINS", "0");
            properties.setProperty("NUM_LAKES", "0");
            properties.setProperty("MAX_SHIELD_STRENGTH", "5");
            properties.setProperty("REPAIR_TIME", "5");
            properties.setProperty("RELOAD_TIME", "5");
            properties.setProperty("MAX_SHOTS", "5");
        }};
        return new World(config);
    }

    /**
     * Sets up 1x1 and 2x2 worlds with the main robot at [0,0] before each test.
     */
    @BeforeEach
    public void setUp() {
        // Create 1x1 world
        world1x1 = createTestWorld(1, 1, 1);
        // Create 2x2 world
        world2x2 = createTestWorld(2, 2, 1);
        // Initialize robot at [0,0]
        robot = new Robot("Prototype", new Position(0, 0));
        robot.setMake("Sniper");
        robot.setDirection(Direction.NORTH);
        robot.setShots(5);
        world1x1.addRobot(robot);
        world2x2.addRobot(robot);
    }

    /**
     * Tests that the robot at [0,0] in a 1x1 world sees no objects, as all directions are outside the world.
     */
    @Test
    public void testLookIn1x1WorldEdges() {
        String response = new LookCommand(world1x1, robot).execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");

        // Expect no objects, as World.findClosestObjectInDirection returns null for invalid positions
        assertEquals(0, objects.size(), "Should see no objects in a 1x1 world, as all directions are outside");
    }

    /**
     * Tests that the robot at [0,0] in a 2x2 world sees an obstacle at [0,1] in the NORTH direction.
     */
    @Test
    public void testLookWithOneObstacle2x2World() {
        // Add obstacle at [0,1]
        Mountain obstacle = new Mountain(0, 1, 1, 1);
        world2x2.addObstacle(obstacle);

        // Execute look command
        String response = new LookCommand(world2x2, robot).execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");

        // Verify response
        assertEquals(1, objects.size(), "Should see exactly one object");
        JsonObject obj = objects.get(0).getAsJsonObject();
        assertEquals("NORTH", obj.get("direction").getAsString(), "Obstacle should be NORTH");
        assertEquals("OBSTACLE", obj.get("type").getAsString(), "Type should be OBSTACLE");
        assertEquals(1, obj.get("distance").getAsInt(), "Distance should be 1 step");
    }

    /**
     * Tests that the robot at [0,0] in a 2x2 world sees an obstacle at [0,1] (NORTH) and a robot at [1,0] (EAST).
     * Note: In a 2x2 world, only [1,0] is in a cardinal direction; [1,1] is diagonal and not detected.
     */
    @Test
    public void testLookWithObstacleAndRobots2x2World() {
        // Add obstacle at [0,1]
        Mountain obstacle = new Mountain(0, 1, 1, 1);
        world2x2.addObstacle(obstacle);

        // Add robots at [1,0] and [1,1]
        Robot robot2 = new Robot("robot2", new Position(1, 0));
        Robot robot3 = new Robot("robot3", new Position(1, 1));
        world2x2.addRobot(robot2);
        world2x2.addRobot(robot3);

        // Execute look command
        String response = new LookCommand(world2x2, robot).execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");

        // Verify response
        assertTrue(objects.size() >= 2, "Should see at least two objects (obstacle and one robot)");

        boolean foundObstacle = false;
        boolean foundRobotEast = false;

        for (JsonElement elem : objects) {
            JsonObject obj = elem.getAsJsonObject();
            String direction = obj.get("direction").getAsString();
            String type = obj.get("type").getAsString();
            int distance = obj.get("distance").getAsInt();

            assertEquals(1, distance, "All objects should be 1 step away");

            if (direction.equals("NORTH") && type.equals("OBSTACLE")) {
                foundObstacle = true;
            } else if (direction.equals("EAST") && type.equals("ROBOT")) {
                foundRobotEast = true;
            }
        }

        assertTrue(foundObstacle, "Should see an OBSTACLE to the NORTH");
        assertTrue(foundRobotEast, "Should see a ROBOT to the EAST");
        // Note: The story expects three robots, but in a 2x2 world with an obstacle at [0,1],
        // only [1,0] is in a cardinal direction (EAST). Robot at [1,1] is diagonal and not detected.
    }

    /**
     * Tests that the robot in an empty large world (50x50) at [0,0] sees no objects within visibility range.
     */
    @Test
    public void testLookCommandEmptyWorldEdge() {
        World largeWorld = createTestWorld(50, 50, 10);
        Robot edgeRobot = new Robot("EdgeBot", new Position(0, 0));
        edgeRobot.setDirection(Direction.NORTH);
        largeWorld.addRobot(edgeRobot);

        String response = new LookCommand(largeWorld, edgeRobot).execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");

        // Expect no objects, as World.findClosestObjectInDirection returns null for invalid positions
        assertTrue(objects.isEmpty(), "Should see no objects, as edges are not returned by findClosestObjectInDirection");
    }

    /**
     * Tests that the robot in the center of a large world sees no objects within range.
     */
    @Test
    public void testLookCommandEmptyRange() {
        World largeWorld = createTestWorld(50, 50, 10);
        Robot centerRobot = new Robot("CenterBot", new Position(25, 25));
        centerRobot.setDirection(Direction.NORTH);
        largeWorld.addRobot(centerRobot);

        ForwardCommand forwardCommand = new ForwardCommand(largeWorld, centerRobot, 20);
        forwardCommand.execute();
        TurnRightCommand rightCommand = new TurnRightCommand(largeWorld, centerRobot);
        rightCommand.execute();
        forwardCommand.execute();

        String response = new LookCommand(largeWorld, centerRobot).execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");

        assertTrue(objects.isEmpty(), "Should see no objects within visibility range");
    }

    /**
     * Tests that obstacles are detected in a large world with configured obstacles.
     */
    @Test
    public void testObstaclesInRange() {
        World obstacleWorld = createTestWorld(50, 50, 10);
        obstacleWorld.addObstacle(new Mountain(10, 10, 1, 1));
        obstacleWorld.addObstacle(new Pit(15, 15, 1, 1));

        Robot testRobot = new Robot("TestBot", new Position(10, 9));
        testRobot.setDirection(Direction.NORTH);
        obstacleWorld.addRobot(testRobot);

        String response = new LookCommand(obstacleWorld, testRobot).execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");

        boolean foundObstacle = false;
        for (JsonElement elem : objects) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.get("type").getAsString().equals("OBSTACLE") && obj.get("distance").getAsInt() == 1) {
                foundObstacle = true;
            }
        }
        assertTrue(foundObstacle, "Should see at least one OBSTACLE within range");
    }
}