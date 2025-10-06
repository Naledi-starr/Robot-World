package za.co.wethinkcode.client;

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

public class RobotMovementTests1x1 {
    private Robot robot;
    private World world;

    private World createTestWorldTwo(int width1, int height1) {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH", String.valueOf(width1));
            properties.setProperty("WORLD_HEIGHT", String.valueOf(height1));
            properties.setProperty("NUM_PITS", "0");
            properties.setProperty("NUM_MOUNTAINS", "0");
            properties.setProperty("NUM_LAKES", "0");
            properties.setProperty("VISIBILITY_RANGE", "5");
            properties.setProperty("MAX_SHIELD_STRENGTH", "5");
            properties.setProperty("REPAIR_TIME", "5");
            properties.setProperty("RELOAD_TIME", "5");
        }};
        return new World(config);
    }

    @BeforeEach
    public void setUp() {
        world = createTestWorldTwo(100, 100);
        robot = new Robot("TestBot", new Position(50, 50)); //IF IT DOESNT PASS USE (50,50)
        robot.setDirection(Direction.NORTH);
        world.addRobot(robot);

    }

    private void assertPosition(int x, int y) {
        assertEquals(new Position(x, y), robot.getPosition());
    }

    private void assertResponseMessage(String response, String expected) {
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        assertEquals(expected, json.getAsJsonObject("data").get("message").getAsString(),
                "Response should contain message: " + expected);
    }

    // ===== BASIC MOVEMENT TESTS =====

    @Test
    public void forwardNorthMovesPositiveY() {
        robot.setDirection(Direction.NORTH);
        new ForwardCommand(world, robot, 5).execute();
        assertPosition(50, 55);
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
        robot.setPosition(new Position(50, 98));
        String response = new ForwardCommand(world, robot, 1).execute();
        assertPosition(50, 99);
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
    }
}