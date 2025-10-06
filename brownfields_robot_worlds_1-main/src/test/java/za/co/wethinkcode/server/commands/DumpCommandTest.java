package za.co.wethinkcode.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

public class DumpCommandTest {
    private World world;
    private DumpCommand dumpCommand;

    private World createTestWorld(int width, int height) {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH",  String.valueOf(width));
            properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
            properties.setProperty("NUM_PITS",      "0");
            properties.setProperty("NUM_MOUNTAINS","0");
            properties.setProperty("NUM_LAKES",     "0");
            properties.setProperty("VISIBILITY_RANGE","5");
            properties.setProperty("MAX_SHIELD_STRENGTH","5");
            properties.setProperty("REPAIR_TIME","5");
            properties.setProperty("RELOAD_TIME","5");
        }};
        return new World(config);
    }

    @BeforeEach
    void setUp() {
        world = createTestWorld(10, 5);
        dumpCommand = new DumpCommand(world);
    }

    @Test
    void testExecuteProducesCorrectJson() {
        String json = dumpCommand.execute();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        assertEquals("OK",      root.get("result").getAsString(),  "Result should be OK");
        assertTrue  (root.has("data"),                           "Should have a data field");

        JsonObject data = root.getAsJsonObject("data");
        assertEquals(10, data.get("width").getAsInt(),  "Width should match world");
        assertEquals(5,  data.get("height").getAsInt(), "Height should match world");

        // Initially no obstacles or robots
        assertEquals(0, data.getAsJsonArray("obstacles").size(), "No obstacles yet");
        assertEquals(0, data.getAsJsonArray("robots").size(),    "No robots yet");
    }

    @Test
    void testDisplay_NoObstaclesNoRobots() {
        String output = dumpCommand.display();

        assertTrue(output.contains("🌍 WORLD DUMP"), "Header");

        assertTrue(output.contains("Size: 10 x 5"),                "Width and height printed");
        assertTrue(output.contains("No obstacles in world"),   "No obstacles in world");
        assertTrue(output.contains("No robots in world"),      "No robots in world");
    }


    @Test
    void testDisplay_WithOneRobot() {
        Robot r = new Robot("R2D2", new Position(-1, 4));
        r.setDirection(Direction.EAST);
        // make, shields, shots have defaults or you can set them:
        r.setMake("Basic");
        r.setShields(3);
        r.setShots(2);
        world.addRobot(r);

        String output = dumpCommand.display();

        // It should no longer show "No robots", and include a line for R2D2
        assertFalse(output.contains("No robots in world"),   "Should not show empty-robots");
        assertTrue(output.contains("R2D2"),                  "Robot name");
        assertTrue(output.contains("Basic"),                 "Robot model");
        assertTrue(output.contains("E"),                     "Direction short form");
        assertTrue(output.contains("3"),                     "Shields");
        assertTrue(output.contains("2"),                     "Shots");
    }
}