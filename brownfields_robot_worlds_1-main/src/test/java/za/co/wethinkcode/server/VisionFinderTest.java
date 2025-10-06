package za.co.wethinkcode.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.utils.VisionFinder;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Lake;
import za.co.wethinkcode.server.world.obstacles.Pit;

import static org.junit.jupiter.api.Assertions.*;

public class VisionFinderTest {

    private World world;
    private Robot robot;
    private VisionFinder visionFinder;

    @BeforeEach
    void setUp() {
        WorldConfig config = new WorldConfig(); // small world
        world = new World(config);

        robot = new Robot("Optimus", new Position(10, 10));

        robot.setDirection(Direction.NORTH);
        world.getRobots().add(robot);

        // Clear generated obstacles and manually set known ones
        world.getObstacles().clear();

        // Place obstacles within visibility
        world.getObstacles().add(new Mountain(10, 7, 1, 1)); // North, 3 steps
        world.getObstacles().add(new Pit(13, 10, 1, 1));     // East, 3 steps
        world.getObstacles().add(new Lake(10, 14, 1, 1));     // South, 4 steps
        world.getObstacles().add(new Mountain(7, 10, 1, 1));  // West, 3 steps

        visionFinder = new VisionFinder(world, robot);
    }

    @Test
    void testObjectsWithinVisibilityAreDetected() {
        JsonObject result = visionFinder.findInAbsoluteDirections(robot);
        JsonArray objects = result.getAsJsonArray("objects");

        assertEquals(4, objects.size(), "Should detect 4 objects in 4 directions");

        boolean northDetected = false, eastDetected = false, southDetected = false, westDetected = false;

        for (int i = 0; i < objects.size(); i++) {
            JsonObject obj = objects.get(i).getAsJsonObject();
            String dir = obj.get("direction").getAsString();
            int distance = obj.get("distance").getAsInt();

            switch (dir) {
                case "NORTH" -> {
                    northDetected = true;
                    assertEquals(3, distance);
                    assertEquals("MOUNTAIN", obj.get("type").getAsString());
                }
                case "EAST" -> {
                    eastDetected = true;
                    assertEquals(3, distance);
                    assertEquals("BOTTOMLESSPIT", obj.get("type").getAsString());
                }
                case "SOUTH" -> {
                    southDetected = true;
                    assertEquals(4, distance);
                    assertEquals("LAKE", obj.get("type").getAsString());
                }
                case "WEST" -> {
                    westDetected = true;
                    assertEquals(3, distance);
                    assertEquals("MOUNTAIN", obj.get("type").getAsString());
                }
            }
        }

        assertTrue(northDetected && eastDetected && southDetected && westDetected, "All directions should be reported");
    }

    @Test
    void testNoObjectBeyondVisibilityRange() {
        // Add obstacle beyond range
        world.getObstacles().add(new Mountain(10, 0, 1, 1)); // North, distance = 10
        JsonObject result = visionFinder.findInAbsoluteDirections(robot);
        JsonArray objects = result.getAsJsonArray("objects");

        for (int i = 0; i < objects.size(); i++) {
            JsonObject obj = objects.get(i).getAsJsonObject();
            assertTrue(obj.get("distance").getAsInt() <= world.getVisibilityRange(), "No object should be beyond visibility range");
        }
    }

    @Test
    void testEdgeIsDetected() {
        robot.setPosition(new Position(0, 0)); // Bottom-left corner
        visionFinder = new VisionFinder(world, robot);

        JsonObject result = visionFinder.findInAbsoluteDirections(robot);
        JsonArray objects = result.getAsJsonArray("objects");

        boolean north = false, west = false;

        for (int i = 0; i < objects.size(); i++) {
            JsonObject obj = objects.get(i).getAsJsonObject();
            String dir = obj.get("direction").getAsString();
            String type = obj.get("type").getAsString();
            int dist = obj.get("distance").getAsInt();

            if (dir.equals("NORTH") && type.equals("EDGE")) north = true;
            if (dir.equals("WEST") && type.equals("EDGE")) west = true;

            assertEquals(1, dist, "Edge should be detected at 1 step away");
        }

        assertTrue(north && west, "Edges should be detected when robot is at boundary");
    }
}
