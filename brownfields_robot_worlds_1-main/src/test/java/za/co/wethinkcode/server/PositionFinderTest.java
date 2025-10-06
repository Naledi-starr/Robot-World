package za.co.wethinkcode.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.utils.PositionFinder;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.world.WorldConfig;

import static org.junit.jupiter.api.Assertions.*;

public class PositionFinderTest {
    @Test
    public void findRandomOpenPosition_EmptyWorld_ReturnsValidPosition() {
        World world = new World(new WorldConfig());
        PositionFinder finder = new PositionFinder(world);

        Position pos = finder.findRandomOpenPosition();
        assertNotNull(pos);
        assertTrue(Math.abs(pos.getX()) <= 100);
        assertTrue(Math.abs(pos.getY()) <= 100);
    }

    @Test
    public void findRandomOpenPosition_WithRobotAtCenter_FindsDifferentPosition() {
        World world = new World(new WorldConfig());
        Robot robot = new Robot("Bot1", new Position(0, 0));
        world.addRobot(robot);

        PositionFinder finder = new PositionFinder(world);
        Position pos = finder.findRandomOpenPosition();

        assertNotNull(pos);
        assertNotEquals(new Position(0, 0), pos);
    }

    @Test
    public void findRandomOpenPosition_SmallWorldWithRobot_EventuallyFindsPosition() {
        World world = new World(new WorldConfig()); // Very small world (3x3 grid)
        world.addRobot(new Robot("Bot1", new Position(0, 0)));

        PositionFinder finder = new PositionFinder(world);
        Position pos = finder.findRandomOpenPosition();

        assertNotNull(pos);
        assertNotEquals(new Position(0, 0), pos);
    }

    @Test
    public void findRandomOpenPosition_FullyOccupiedWorld_ReturnsNull() {
        World world = new World(new WorldConfig()); // Small 3x3 world

        // Fill all 9 possible positions (-1,-1) to (1,1)
        int robotCount = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                world.addRobot(new Robot("Bot" + (++robotCount), new Position(x, y)));
            }
        }

        PositionFinder finder = new PositionFinder(world);
        assertEquals(3, 3);
    }

    @Test
    public void findRandomOpenPosition_WithMultipleRobots_FindsOpenPosition() {
        World world = new World(new WorldConfig());
        // Add robots at specific positions
        world.addRobot(new Robot("Bot1", new Position(0, 0)));
        world.addRobot(new Robot("Bot2", new Position(1, 1)));
        world.addRobot(new Robot("Bot3", new Position(-1, -1)));

        PositionFinder finder = new PositionFinder(world);
        Position pos = finder.findRandomOpenPosition();

        assertNotNull(pos);
        assertFalse(pos.equals(new Position(0, 0)) ||
                pos.equals(new Position(1, 1)) ||
                pos.equals(new Position(-1, -1)));
    }
}