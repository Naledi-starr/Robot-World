package za.co.wethinkcode.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.Mountain;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class WorldTest {
    private World world;
    private Properties properties;

    @BeforeEach
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("WORLD_WIDTH", "20");
        props.setProperty("WORLD_HEIGHT", "20");
        props.setProperty("NUM_PITS", "0");
        props.setProperty("NUM_LAKES", "0");
        props.setProperty("NUM_MOUNTAINS", "0");
        props.setProperty("VISIBILITY_RANGE", "5");
        props.setProperty("MAX_SHIELD_STRENGTH", "5");
        props.setProperty("REPAIR_TIME", "5");
        props.setProperty("RELOAD_TIME", "5");
        props.setProperty("MAX_SHOTS", "10");


        WorldConfig config = new WorldConfig(props);
        world = new World(config);
    }

    @Test
    public void testWorldConstruction() {
        assertEquals(20, world.getWidth());
        assertEquals(20, world.getHeight());
        assertEquals(5, world.getVisibilityRange());
        assertEquals(0, world.getObstacles().size());
        assertEquals(0, world.getRobots().size());
    }

    @Test
    public void testAddRobotAndGetByName() {
        Robot robot = new Robot("Robo1", new Position(0, 0));
        world.addRobot(robot);

        Robot fetched = world.getRobotByName("Robo1");
        assertNotNull(fetched);
        assertEquals(robot, fetched);
    }

    @Test
    public void testRemoveRobot() {
        Robot robot = new Robot("Robo1", new Position(0, 0));
        world.addRobot(robot);
        world.removeRobot(robot);

        assertNull(world.getRobotByName("Robo1"));
        assertEquals(0, world.getRobots().size());
    }

    @Test
    public void testIsPositionValidWithinBounds() {
        assertTrue(world.isPositionValid(new Position(0, 0)));
        //assertTrue(world.isPositionValid(new Position(-10, -10)));
        assertTrue(world.isPositionValid(new Position(10, 10)));
    }

    @Test
    public void testIsPositionValidOutsideBounds() {
        assertTrue(world.isPositionValid(new Position(11, 0)));
        assertTrue(world.isPositionValid(new Position(0, 11)));
        //assertFalse(world.isPositionValid(new Position(-11, 0)));
        assertFalse(world.isPositionValid(new Position(0, -11)));
    }

    @Test
    public void testIsPositionBlockedByObstacle() {
        Mountain mountain = new Mountain(1, 1, 2, 2);
        world.getObstacles().add(mountain);  // Direct access for test only

        assertTrue(world.isPositionBlocked(new Position(1, 1)));
        assertTrue(world.isPositionBlocked(new Position(2, 2)));
        assertFalse(world.isPositionBlocked(new Position(3, 3)));
    }

    @Test
    public void testGetVisibleObstacles() {
        Mountain mountain = new Mountain(1, 3, 3, 3);
        world.getObstacles().add(mountain);

        Robot robot = new Robot("Observer", new Position(1, 1));
        robot.setDirection(Direction.NORTH);
        world.addRobot(robot);

        List<?> visible = world.getVisibleObstacles(robot);
        assertTrue(visible.contains(mountain));
    }

    @Test
    public void testGetVisibleObstacles_NotVisible() {
        Mountain mountain = new Mountain(2, 4, 3, 3);
        world.getObstacles().add(mountain);

        Robot robot = new Robot("Observer", new Position(0, 0));
        robot.setDirection(Direction.NORTH);
        world.addRobot(robot);

        List<?> visible = world.getVisibleObstacles(robot);
        assertFalse(visible.contains(mountain));
    }
}


