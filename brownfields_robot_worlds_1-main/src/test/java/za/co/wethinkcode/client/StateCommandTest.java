package za.co.wethinkcode.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.commands.*;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateCommandTest {

    private World world;
    private Robot robot;

    /**
     *Using the following method, we create a world with specified dimensions
     * Inside the method properties for the world constraints and contains are set
     */
    private World createTestWorld(int width, int height) {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH", String.valueOf(width));
            properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
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
        // Initialize mock World
        world = createTestWorld(10, 10);

        // Initialize the shooter robot
        robot = new Robot("Prototype", new Position(0, 0));
        robot.setMake("Sniper");
        robot.setDirection(Direction.NORTH);
        robot.setShots(5);
        world.addRobot(robot);
    }

    /**
     *The following set of test the initial state before any commands are executed on the robot.
     */

    @Test
    public void stateAndSetUpMatchPosition() {
        StateCommand stateCommand = new StateCommand(robot);
        JsonObject jsonState = stateCommand.toJson();
        JsonElement position = jsonState.get("position");
        JsonArray positionString = position.getAsJsonArray();
        double xPosition = positionString.get(0).getAsDouble();
        double yPosition = positionString.get(1).getAsDouble();
        assertEquals(xPosition, robot.getPosition().getX());
        assertEquals(yPosition, robot.getPosition().getY());
    }

    @Test
    public void stateAndSetUpMatchDirection() {
        StateCommand stateCommand = new StateCommand(robot);
        JsonObject jsonState = stateCommand.toJson();
        JsonElement direction = jsonState.get("direction");
        String initialDirection = direction.getAsString();
        assertEquals(initialDirection, robot.getDirection().name());
    }

    @Test
    public void stateAndSetUpMatchShots() {
        StateCommand stateCommand = new StateCommand(robot);
        JsonObject jsonState = stateCommand.toJson();
        JsonElement shots = jsonState.get("shots");
        int initialShots = shots.getAsInt();
        assertEquals(initialShots, robot.getShots());
    }

    @Test
    public void stateAndSetUpMatchShields() {
        StateCommand stateCommand = new StateCommand(robot);
        JsonObject jsonState = stateCommand.toJson();
        JsonElement shields = jsonState.get("shields");
        int initialShields = shields.getAsInt();
        assertEquals(initialShields, robot.getShields());
    }

    /**
     *The following tests will be testing the state after a set of commands are executed on the robot.
     */

    @Test
    public void testShotsIndicatedonStateCommand() {
        FireCommand fireCommand = new FireCommand(world, robot);
        fireCommand.execute();
        fireCommand.execute();
        StateCommand stateCommand = new StateCommand(robot);
        JsonObject jsonState = stateCommand.toJson();
        JsonElement shots = jsonState.get("shots");
        int shotsLeft = shots.getAsInt();
        assertEquals(shotsLeft, robot.getShots());
        assertEquals(3, shotsLeft); // Shot executed 2 times after initialisation at 5 shots
    }

    @Test
    public void testMovementTrackedByStateCommand() {
        ForwardCommand forwardCommand = new ForwardCommand(world, robot, 7);
        forwardCommand.execute();
        TurnRightCommand rightCommand = new TurnRightCommand(world, robot);
        rightCommand.execute();
        ForwardCommand forwardCommand2 = new ForwardCommand(world, robot, 5);
        forwardCommand2.execute();
        StateCommand stateCommand = new StateCommand(robot);
        JsonObject jsonState = stateCommand.toJson();
        JsonElement Position = jsonState.get("position");
        JsonArray positionString = Position.getAsJsonArray();
        assertEquals(5, positionString.get(0).getAsDouble());
        assertEquals(7, positionString.get(1).getAsDouble());
    }
}
