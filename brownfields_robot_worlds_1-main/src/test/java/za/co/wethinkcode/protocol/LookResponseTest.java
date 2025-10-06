package za.co.wethinkcode.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.commands.LookCommand;
import za.co.wethinkcode.client.commands.StateCommand;
import za.co.wethinkcode.protocol.client.LookResponse;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.Mountain;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class LookResponseTest {


    private World testWorld;
    private Robot testRobot;

    // Helper method to verify common response structure
    private void verifyCommonResponseStructure(JsonObject json, int expectedObjectCount, int expectedVisibility) {
        verifyTopLevelStructure(json);
        JsonObject data = json.getAsJsonObject("data");
        JsonArray objects = data.getAsJsonArray("objects");
        assertEquals(expectedObjectCount, objects.size(), "Unexpected number of objects");
        assertEquals(expectedVisibility, data.get("visibilityRange").getAsInt(), "Visibility range mismatch");
        verifyRobotState(json.getAsJsonObject("state"), 0, 0);
    }

    // Helper method to check if an object matches expected properties
    private boolean isMatchingObject(JsonObject obj, String direction, String type, int distance) {
        return obj.get("direction").getAsString().equals(direction) &&
                obj.get("type").getAsString().equals(type) &&
                obj.get("distance").getAsInt() == distance;
    }

    /**
     * Sets up a 2x2 world and a test robot at [0,0] for all tests.
     */
    @BeforeEach
    void setUp() {
        // Configure a 2x2 world with visibility range 1
        Properties props = new Properties();
        props.setProperty("WORLD_WIDTH", "2");
        props.setProperty("WORLD_HEIGHT", "2");
        props.setProperty("VISIBILITY_RANGE", "1");
        props.setProperty("NUM_MOUNTAINS", "0");
        props.setProperty("NUM_LAKES", "0");
        props.setProperty("NUM_PITS", "0");
        props.setProperty("MAX_SHIELD_STRENGTH", "5");
        props.setProperty("RELOAD_TIME", "5");
        props.setProperty("REPAIR_TIME", "5");
        props.setProperty("MAX_SHOTS", "5");
        WorldConfig config = new WorldConfig(props);
        testWorld = new World(config);

        // Create test robot at [0,0]
        testRobot = new Robot("TestBot", new Position(0, 0));
        testRobot.setDirection(Direction.NORTH);
        testRobot.setStatus(Robot.Status.NORMAL);
        testWorld.addRobot(testRobot);
    }

    /**
     * Creates a LookResponse with the given world, robot, and mock objects.
     */
    private LookResponse createLookResponse(World world, Robot robot, String... objectSpecs) {
        LookCommand lookCommand = new LookCommand(world, robot);
        for (int i = 0; i < objectSpecs.length; i += 3) {
            lookCommand.addObject(objectSpecs[i], objectSpecs[i + 1], Integer.parseInt(objectSpecs[i + 2]));
        }
        StateCommand stateCommand = new StateCommand(robot);
        return new LookResponse(lookCommand, stateCommand);
    }

    /**
     * Verifies the top-level structure of the JSON response.
     */
    private void verifyTopLevelStructure(JsonObject response) {
        assertTrue(response.has("result"), "Response should have 'result' field");
        assertTrue(response.has("data"), "Response should have 'data' field");
        assertTrue(response.has("state"), "Response should have 'state' field");
        assertEquals("OK", response.get("result").getAsString(), "Result should be 'OK'");
    }

    /**
     * Verifies the robot's state in the JSON response.
     */
    private void verifyRobotState(JsonObject state, int expectedX, int expectedY) {
        assertTrue(state.has("position"), "State should have 'position' field");
        JsonElement positionElement = state.get("position");
        if (positionElement.isJsonArray()) {
            JsonArray positionArray = positionElement.getAsJsonArray();
            assertEquals(2, positionArray.size(), "Position should be an array of size 2");
            assertEquals(expectedX, positionArray.get(0).getAsInt(), "X coordinate should match");
            assertEquals(expectedY, positionArray.get(1).getAsInt(), "Y coordinate should match");
        } else {
            assertEquals("[" + expectedX + "," + expectedY + "]", positionElement.getAsString(), "Position string should match");
        }
        assertEquals("NORTH", state.get("direction").getAsString(), "Direction should be NORTH");
        assertEquals("NORMAL", state.get("status").getAsString(), "Status should be NORMAL");
    }

    /**
     * Verifies an object's properties in the JSON response.
     */
    private void verifyObject(JsonObject object, String expectedDirection, String expectedType, int expectedDistance) {
        assertEquals(expectedDirection, object.get("direction").getAsString(), "Direction should match");
        assertEquals(expectedType, object.get("type").getAsString(), "Type should match");
        assertEquals(expectedDistance, object.get("distance").getAsInt(), "Distance should match");
    }

    /**
     * Tests the robot at [0,0] seeing an obstacle at [0,1] in a 2x2 world.
     */
    @Test
    public void testLookWithOneObstacle() {
        // Add obstacle at [0,1]
        Mountain obstacle = new Mountain(0, 1, 1, 1);
        testWorld.addObstacle(obstacle);

        // Execute look command
        LookCommand lookCommand = new LookCommand(testWorld, testRobot);
        String response = lookCommand.execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        // Verify response structure
        verifyTopLevelStructure(json);
        JsonObject data = json.getAsJsonObject("data");
        JsonArray objects = data.getAsJsonArray("objects");

        // Verify objects
        assertEquals(1, objects.size(), "Should see exactly one object");
        JsonObject obj = objects.get(0).getAsJsonObject();
        verifyObject(obj, "NORTH", "OBSTACLE", 1);

        // Verify state
        verifyRobotState(json.getAsJsonObject("state"), 0, 0);
    }

    /**
     * Tests the robot at [0,0] seeing an obstacle at [0,1] and robots at [1,0], [1,1] in a 2x2 world.
     * Note: Only [1,0] is in a cardinal direction (EAST), so we expect 2 objects (obstacle and one robot).
     */
    @Test
    public void testLookWithObstacleAndRobots() {
        // Add obstacle at [0,1]
        Mountain obstacle = new Mountain(0, 1, 1, 1);
        testWorld.addObstacle(obstacle);

        // Add robots at [1,0] and [1,1]
        Robot robot2 = new Robot("robot2", new Position(1, 0));
        Robot robot3 = new Robot("robot3", new Position(1, 1));
        testWorld.addRobot(robot2);
        testWorld.addRobot(robot3);

        // Execute look command
        LookCommand lookCommand = new LookCommand(testWorld, testRobot);
        String response = lookCommand.execute();
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        // Verify response structure
        verifyTopLevelStructure(json);
        JsonObject data = json.getAsJsonObject("data");
        JsonArray objects = data.getAsJsonArray("objects");

        // Verify objects
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
        // Note: Robot at [1,1] is diagonal and not detected in a cardinal direction.
        // The story expects three robots, but a 2x2 world limits us to one robot in a cardinal direction.

        // Verify state
        verifyRobotState(json.getAsJsonObject("state"), 0, 0);
    }

    /**
     * Tests JSON structure for an empty world.
     */
    @Test
    public void testEmptyWorld() {
        LookResponse response = createLookResponse(testWorld, testRobot);
        JsonObject json = response.toJson();
        verifyCommonResponseStructure(json, 0, 1);
    }

    /**
     * Tests JSON structure for one obstacle in view.
     */
    @Test
    public void testObjectInView() {
        LookResponse response = createLookResponse(testWorld, testRobot, "NORTH", "OBSTACLE", "1");
        JsonObject json = response.toJson();
        verifyCommonResponseStructure(json, 1, 1);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");
        verifyObject(objects.get(0).getAsJsonObject(), "NORTH", "OBSTACLE", 1);
    }

    /**
     * Tests JSON structure for one robot in view.
     */

    @Test
    public void testRobotInView() {
        LookResponse response = createLookResponse(testWorld, testRobot, "EAST", "ROBOT", "1");
        JsonObject json = response.toJson();
        verifyCommonResponseStructure(json, 1, 1);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");
        verifyObject(objects.get(0).getAsJsonObject(), "EAST", "ROBOT", 1);
    }

    /**
     * Tests JSON structure for multiple objects in view.
     */
    @Test
    public void testMultipleObjectsInView() {
        LookResponse response = createLookResponse(testWorld, testRobot, "NORTH", "OBSTACLE", "1", "EAST", "ROBOT", "1");
        JsonObject json = response.toJson();
        verifyCommonResponseStructure(json, 2, 1);
        JsonArray objects = json.getAsJsonObject("data").getAsJsonArray("objects");

        boolean foundObstacle = false;
        boolean foundRobot = false;
        for (JsonElement elem : objects) {
            JsonObject obj = elem.getAsJsonObject();
            if (isMatchingObject(obj, "NORTH", "OBSTACLE", 1)) {
                foundObstacle = true;
            } else if (isMatchingObject(obj, "EAST", "ROBOT", 1)) {
                foundRobot = true;
            }
        }
        assertTrue(foundObstacle, "Should see an OBSTACLE to the NORTH");
        assertTrue(foundRobot, "Should see a ROBOT to the EAST");
    }
}