package za.co.wethinkcode.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.robots.Robot;
import za.co.wethinkcode.server.model.Direction;

public class RobotTest {
    private Robot robot;

    @BeforeEach
    void setUp() {
        robot = new Robot("TestBot", "Sniper", 0, 0, Direction.NORTH);
    }

    @Test
    public void constructor_InitializesCorrectly() {
        assertEquals("TestBot", robot.getName());
        assertEquals("Sniper", robot.getMake());
        assertEquals(0, robot.getX());
        assertEquals(Direction.NORTH, robot.getDirection());
    }

    @Test
    public void toString_ReturnsFormattedString() {
        String expected = "Robot[name=TestBot, make=Sniper, position=(0,0), facing=NORTH]";
        assertEquals(expected, robot.toString());
    }
}