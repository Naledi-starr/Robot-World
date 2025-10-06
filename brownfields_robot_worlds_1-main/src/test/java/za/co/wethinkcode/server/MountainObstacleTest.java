package za.co.wethinkcode.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.server.commands.DumpCommand;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.Mountain;

public class MountainObstacleTest {

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
        }};
        return new World(config);
    }

    @BeforeEach
    void setUp() {
        world = createTestWorld(10, 5);  // Create a world with dimensions 10x5
        dumpCommand = new DumpCommand(world);  // Create the DumpCommand for the world
    }

    @Test
    void testDisplay_WithMountainObstacle() {
        world.addObstacle(new Mountain(2, 3, 1, 1));  // Pass all required args

        String output = dumpCommand.display();

        // Match the type returned by Mountain.getType()
        assertTrue(output.contains("Mountain"), "Should mention obstacle type");
        assertTrue(output.contains("(  2,  3)"), "Should mention obstacle's coordinates");
        assertFalse(output.contains("No obstacles in world"), "Should not show empty-obstacles message");
    }


    @Test
    void testMountainObstacleType() {
        // Create a Mountain obstacle with valid constructor args
        Mountain obstacle = new Mountain(5, 5, 1, 1);

        // Verify that the type is correctly set as "Mountain"
        assertEquals("Mountain", obstacle.getType(), "Obstacle type should be 'Mountain'");
    }


    @Test
    void testMountainObstacleVisibilityBlocking() {
        // Create a Mountain obstacle with required dimensions
        Mountain obstacle = new Mountain(5, 5, 1, 1);

        // Check that it blocks visibility
        assertTrue(obstacle.blocksVisibility(), "Mountain should block visibility");
    }
}

