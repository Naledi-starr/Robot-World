package za.co.wethinkcode.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.world.obstacles.Pit;

import static org.junit.jupiter.api.Assertions.*;

public class PitObstacleTest {

    @Test
    void testPitObstacleConstruction() {
        Pit pit = new Pit(2, 3, 1, 1);
        assertNotNull(pit, "Pit should be created");
    }

    @Test
    void testPitObstacleType() {
        Pit pit = new Pit(4, 5, 1, 1);
        assertEquals("BottomlessPit", pit.getType(), "Obstacle type should be BottomlessPit");
    }

    @Test
    void testPitObstacleBlocksVisibility() {
        Pit pit = new Pit(5, 6, 1, 1);
        assertFalse(pit.blocksVisibility(), "Pit should NOT block visibility");
    }

    @Test
    void testPitObstaclePosition() {
        Pit pit = new Pit(7, 8, 2, 2);
        assertEquals(7, pit.getBottomLeftX(), "X position should be correct");
        assertEquals(10, pit.getBottomLeftY(), "Y position should be correct");
    }
}

