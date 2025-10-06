package za.co.wethinkcode.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.robots.Robot;
import za.co.wethinkcode.client.robots.RobotMaker;

public class RobotMakerTest {
    @Test
    public void createRobot_ValidInput_ReturnsRobot() {
        RobotMaker maker = new RobotMaker();
        Robot robot = maker.createRobot("Bot1", "Defender");

        assertEquals("Bot1", robot.getName());
        assertEquals("Defender", robot.getMake());
        assertEquals(0, robot.getX()); // Default position
    }

    @Test
    public void getStandardMakes_ReturnsCorrectArray() {
        String[] expected = {"Sniper", "Defender", "Trunk", "Razor"};
        assertArrayEquals(expected, new RobotMaker().getStandardMakes());
    }
}