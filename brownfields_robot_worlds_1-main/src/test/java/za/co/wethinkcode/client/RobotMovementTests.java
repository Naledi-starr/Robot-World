package za.co.wethinkcode.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.client.commands.BackCommand;
import za.co.wethinkcode.client.commands.ForwardCommand;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

public class RobotMovementTests {
    private Robot robot;
    private World world;

    @BeforeEach
    public void setUp() {
        WorldConfig config = new WorldConfig();
        world = new World(config);
        robot = new Robot("TestBot", new Position(50, 50));
        robot.setDirection(Direction.NORTH);
        world.addRobot(robot);
    }

    private void assertPosition(int x, int y) {
        assertEquals(new Position(x, y), robot.getPosition());
    }

    private void assertResponseMessage(String response, String expected) {
        assertTrue(response.contains("\"message\":\"" + expected + "\""),
                "Response should contain message: " + expected);
    }

    // ===== BASIC MOVEMENT TESTS =====

    @Test
    public void forwardNorthMovesPositiveY() {
        robot.setDirection(Direction.NORTH);
        new ForwardCommand(world, robot, 5).execute();
        assertPosition(50,55);
    }

    @Test
    public void forwardSouthMovesNegativeY() {
        robot.setDirection(Direction.SOUTH);
        new ForwardCommand(world, robot, 3).execute();
        assertPosition(50, 47);
    }

    @Test
    public void forwardEastMovesPositiveX() {
        robot.setDirection(Direction.EAST);
        new ForwardCommand(world, robot, 2).execute();
        assertPosition(52, 50);
    }

    @Test
    public void forwardWestMovesNegativeX() {
        robot.setDirection(Direction.WEST);
        new ForwardCommand(world, robot, 4).execute();
        assertPosition(46, 50);
    }

    @Test
    public void backNorthMovesNegativeY() {
        robot.setDirection(Direction.NORTH);
        String response = new BackCommand(world, robot, 5).execute();
        assertPosition(50, 45);
        assertResponseMessage(response, "Done");
    }

    @Test
    public void backSouthMovesPositiveY() {
        robot.setDirection(Direction.SOUTH);
        new BackCommand(world, robot, 3).execute();
        assertPosition(50, 53);
    }

    @Test
    public void backEastMovesNegativeX() {
        robot.setDirection(Direction.EAST);
        new BackCommand(world, robot, 2).execute();
        assertPosition(48, 50);
    }

    @Test
    public void backWestMovesPositiveX() {
        robot.setDirection(Direction.WEST);
        new BackCommand(world, robot, 4).execute();
        assertPosition(54, 50);
    }

    // ===== BOUNDARY TESTS =====

    @Test
    public void forwardToNorthBoundary() {
        robot.setDirection(Direction.NORTH);
        robot.setPosition(new Position(50, 1));
        String response = new ForwardCommand(world, robot, 1).execute();
        assertPosition(50, 2);
        assertResponseMessage(response, "Done");
    }

    @Test
    public void backBlockedAtNorthBoundary() {
        robot.setDirection(Direction.NORTH);
        robot.setPosition(new Position(99, 0));
        String response = new BackCommand(world, robot, 1).execute();
        assertPosition(99, 0);
        assertResponseMessage(response, "Obstructed");
    }

    @Test
    public void forwardBackBlockedAtNorthBoundary() {
        robot.setDirection(Direction.NORTH);
        robot.setPosition(new Position(99, 0));
        String response = new ForwardCommand(world, robot, 1).execute();
        assertPosition(99, 1);
        assertResponseMessage(response, "Done");
    }

    @Test
    public void forwardWESTBlockedAtNorthBoundary() {
        robot.setDirection(Direction.NORTH);
        robot.setPosition(new Position(99, 0));
        robot.setDirection(Direction.WEST);
        String response = new ForwardCommand(world, robot, 3).execute();
        assertPosition(96, 0);
        assertResponseMessage(response, "Done");
    }

    @Test
    public void backToSouthBoundary() {
        robot.setDirection(Direction.NORTH);
        robot.setPosition(new Position(50, 98));
        String response = new BackCommand(world, robot, 1).execute();
        assertPosition(50, 97);
        assertResponseMessage(response, "Done");
    }

    @Test
    public void forwardBlockedAtSouthBoundary() {
        robot.setPosition(new Position(50, 99));
        String response = new ForwardCommand(world, robot, 1).execute();
        assertPosition(50, 99);
        assertResponseMessage(response, "Obstructed");
    }

    @Test
    public void backForwardBlockedAtSouthBoundary() {
        robot.setPosition(new Position(50, 99));
        String response = new BackCommand(world, robot, 1).execute();
        assertPosition(50, 98);
        assertResponseMessage(response, "Done");
    }

    // ===== MULTI-STEP MOVEMENT TESTS =====

    @Test
    public void multipleForwardMovements() {
        new ForwardCommand(world, robot, 2).execute();
        assertPosition(50, 52);

        new ForwardCommand(world, robot, 3).execute();
        assertPosition(50, 55);
    }

    @Test
    public void mixedForwardAndBackMovements() {
        new ForwardCommand(world, robot, 5).execute();
        assertPosition(50, 55);

        new BackCommand(world, robot, 3).execute();
        assertPosition(50, 52);

        new ForwardCommand(world, robot, 2).execute();
        assertPosition(50, 54);
    }

    // ===== ROBOT COLLISION TESTS =====

    @Test
    public void forwardBlockedByOtherRobot() {
        Robot other = new Robot("OtherBot", new Position(50, 51));
        world.addRobot(other);
        String response = new ForwardCommand(world, robot, 1).execute();
        assertPosition(50, 50);
        assertResponseMessage(response, "Obstructed");
    }

    @Test
    public void backBlockedByOtherRobot() {
        robot.setDirection(Direction.NORTH);
        Robot other = new Robot("OtherBot", new Position(50, 49));
        world.addRobot(other);
        String response = new BackCommand(world, robot, 1).execute();
        assertPosition(50,50);
        assertResponseMessage(response,"Obstructed");
    }
}