package za.co.wethinkcode.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.protocol.server.ServerCommandProcessor;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

import static org.junit.jupiter.api.Assertions.*;

class CommandProcessorTest {
    private World world;
    private ServerCommandProcessor processor;

    @BeforeEach
    void setUp() {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH", "100");
            properties.setProperty("WORLD_HEIGHT", "100");
            properties.setProperty("NUM_PITS", "0");
            properties.setProperty("NUM_MOUNTAINS", "0");
            properties.setProperty("NUM_LAKES", "0");
            properties.setProperty("VISIBILITY_RANGE", "5");
        }};

        world = new World(config);
        processor = new ServerCommandProcessor(world);
    }

    @Test
    void processMessage_LaunchCommand_ReturnsSuccess() {
        String json = "{\"robot\":\"Bot1\",\"command\":\"launch\",\"arguments\":[\"Sniper\",5,10]}";
        String response = processor.processMessage(json);

        assertTrue(response.contains("\"result\":\"OK\""), "Response should indicate success");
        assertEquals(1, world.getRobots().size(), "World should contain one robot");
        assertEquals("Bot1", world.getRobots().get(0).getName(), "Robot name should match");
    }

    @Test
    void processMessage_InvalidJson_ReturnsError() {
        String response = processor.processMessage("invalid json");
        assertTrue(response.contains("\"result\":\"ERROR\""), "Response should indicate error");
    }

    @Test
    void processMessage_UnknownCommand_ReturnsError() {
        String json = "{\"robot\":\"Bot1\",\"command\":\"unknown\",\"arguments\":[]}";
        String response = processor.processMessage(json);
        assertTrue(response.contains("\"result\":\"ERROR\""), "Unknown command should return error");
    }
}
