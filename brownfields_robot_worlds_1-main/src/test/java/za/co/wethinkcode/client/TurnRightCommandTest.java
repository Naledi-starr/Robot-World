package za.co.wethinkcode.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.client.commands.TurnRightCommand;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.world.World;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import java.util.Properties;

class TurnRightCommandTest {
    private World world;
    private Robot robot;
    private TurnRightCommand command;

    @BeforeEach
    void setUp() {
        world = new World(new WorldConfig());
        robot = new Robot("testBot", new Position(0, 0));
        command = new TurnRightCommand(world, robot);
    }

    @Test
    void getNameShouldReturnRight() {
        assertEquals("right", command.getName());
    }

    @Test
    void displayShouldReturnTurnedRight() {
        assertEquals("Turned right", command.display());
    }

    @Test
    void executeShouldTurnNorthToEast() {
        robot.setDirection(Direction.NORTH);
        String response = command.execute();
        assertEquals(Direction.EAST, robot.getDirection());
        verifyResponse(response, Direction.EAST, new Position(0, 0));
    }

    @Test
    void executeShouldTurnEastToSouth() {
        robot.setDirection(Direction.EAST);
        String response = command.execute();
        assertEquals(Direction.SOUTH, robot.getDirection());
        verifyResponse(response, Direction.SOUTH, new Position(0, 0));
    }

    @Test
    void executeShouldTurnSouthToWest() {
        robot.setDirection(Direction.SOUTH);
        String response = command.execute();
        assertEquals(Direction.WEST, robot.getDirection());
        verifyResponse(response, Direction.WEST, new Position(0, 0));
    }

    @Test
    void executeShouldTurnWestToNorth() {
        robot.setDirection(Direction.WEST);
        String response = command.execute();
        assertEquals(Direction.NORTH, robot.getDirection());
        verifyResponse(response, Direction.NORTH, new Position(0, 0));
    }

    @Test
    void executeAtWorldBoundary() {
        // Place robot at top-right corner of a 2x2 world
        world = new World(new WorldConfig(createCustomConfig(2, 2)));
        robot = new Robot("HAL", new Position(1, 1));
        command = new TurnRightCommand(world, robot);
        robot.setDirection(Direction.NORTH);
        String response = command.execute();
        assertEquals(Direction.EAST, robot.getDirection());
        verifyResponse(response, Direction.EAST, new Position(1, 1));
    }

    @Test
    void executeWithCustomWorldConfig() {
        // Use custom world config with larger size
        Properties props = createCustomConfig(50, 50);
        world = new World(new WorldConfig(props));
        robot = new Robot("testBot", new Position(25, 25));
        command = new TurnRightCommand(world, robot);
        robot.setDirection(Direction.WEST);
        String response = command.execute();
        assertEquals(Direction.NORTH, robot.getDirection());
        verifyResponse(response, Direction.NORTH, new Position(25, 25));
    }

    @Test
    void executeWithMultipleTurns() {
        // Execute turn right multiple times to ensure consistent behavior
        robot.setDirection(Direction.NORTH);
        String response = command.execute();
        assertEquals(Direction.EAST, robot.getDirection());
        response = command.execute();
        assertEquals(Direction.SOUTH, robot.getDirection());
        response = command.execute();
        assertEquals(Direction.WEST, robot.getDirection());
        response = command.execute();
        assertEquals(Direction.NORTH, robot.getDirection());
        verifyResponse(response, Direction.NORTH, new Position(0, 0));
    }

    @Test
    void executeAtRandomPosition() {
        // Test at a random valid position
        world = new World(new WorldConfig(createCustomConfig(10, 10)));
        robot = new Robot("testBot", new Position(5, 5));
        command = new TurnRightCommand(world, robot);
        robot.setDirection(Direction.SOUTH);
        String response = command.execute();
        assertEquals(Direction.WEST, robot.getDirection());
        verifyResponse(response, Direction.WEST, new Position(5, 5));
    }

    private Properties createCustomConfig(int width, int height) {
        Properties props = new Properties();
        props.setProperty("WORLD_WIDTH", String.valueOf(width));
        props.setProperty("WORLD_HEIGHT", String.valueOf(height));
        props.setProperty("NUM_PITS", "0");
        props.setProperty("NUM_LAKES", "0");
        props.setProperty("NUM_MOUNTAINS", "0");
        props.setProperty("VISIBILITY_RANGE", "10");
        props.setProperty("MAX_SHIELD_STRENGTH", "5");
        props.setProperty("REPAIR_TIME", "5");
        props.setProperty("RELOAD_TIME", "5");
        props.setProperty("MAX_SHOTS", "10");
        return props;
    }

    private void verifyResponse(String jsonResponse, Direction expectedDirection, Position expectedPosition) {
        JsonObject response = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Verify result
        assertEquals("OK", response.get("result").getAsString());

        // Verify data message
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Done", data.get("message").getAsString());

        // Verify state direction
        JsonObject state = response.getAsJsonObject("state");
        assertEquals(expectedDirection.toString(), state.get("direction").getAsString());

        // Verify position remains unchanged (position is a JsonArray [x, y])
        JsonArray position = state.getAsJsonArray("position");
        assertEquals(expectedPosition.getX(), position.get(0).getAsInt());
        assertEquals(expectedPosition.getY(), position.get(1).getAsInt());

        // Verify state contains other required fields
        assertTrue(state.has("shields"));
        assertTrue(state.has("shots"));
        assertTrue(state.has("status"));
    }
}