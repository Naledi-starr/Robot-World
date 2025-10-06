package za.co.wethinkcode.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.world.obstacles.Lake;

import static org.junit.jupiter.api.Assertions.*;

public class LakeObstacleTest {

    // Test the construction of Lake
    @Test
    void testLakeObstacleConstruction() {
        Lake lake = new Lake(3, 4, 2, 2);
        assertNotNull(lake, "Lake should be created");
    }

    // Test the type of the Lake obstacle
    @Test
    void testLakeObstacleType() {
        Lake lake = new Lake(5, 6, 2, 2);
        assertEquals("Lake", lake.getType(), "Obstacle type should be Lake");
    }

    // Test if Lake blocks visibility (should not)
    @Test
    void testLakeObstacleBlocksVisibility() {
        Lake lake = new Lake(7, 8, 2, 2);
        assertFalse(lake.blocksVisibility(), "Lake should not block visibility");
    }

    // Test the position of the Lake obstacle
    @Test
    void testLakeObstaclePosition() {
        Lake lake = new Lake(9, 10, 2, 2);
        assertEquals(9, lake.getBottomLeftX(), "X position should be correct");
        assertEquals(12, lake.getBottomLeftY(), "Y position should be correct");
    }
}

