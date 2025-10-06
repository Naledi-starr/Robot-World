package za.co.wethinkcode.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.protocol.client.CommandProcessor;
import za.co.wethinkcode.protocol.server.ServerCommandProcessor;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.client.commands.FireCommand;
import za.co.wethinkcode.client.Play;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ShieldSystemTests {
    // Shared test objects
    private World world;
    private WorldConfig config;
    private CommandProcessor clientCmdProcessor;
    private ServerCommandProcessor serverCmdProcessor;

    @BeforeEach
    void setUp() {
        // Configure world with max shields = 5
        config = new WorldConfig();
        config.properties.setProperty("MAX_SHIELD_STRENGTH", "5");
        world = new World(config);

        // Client setup
        Play playMock = new Play(new Scanner(System.in));
        clientCmdProcessor = new CommandProcessor(playMock);
        clientCmdProcessor.setRobotDetails("sniper", "testBot");

        // Server setup
        serverCmdProcessor = new ServerCommandProcessor(world);
    }

    // --- CLIENT COMMAND TESTS ---
    @Test
    void clientLaunchCommand_FormatsCorrectJson() {
        String json = clientCmdProcessor.convertToJsonCommand("launch sniper testBot");
        JsonObject cmd = new com.google.gson.JsonParser().parse(json).getAsJsonObject();

        assertEquals("launch", cmd.get("command").getAsString());
        JsonArray args = cmd.getAsJsonArray("arguments");
    }


    @Test
    void serverAcceptsValidShields() {
        JsonObject request = createLaunchRequest("testBot", 3, 10);
        String response = serverCmdProcessor.processMessage(request.toString());
        assertTrue(response.contains("\"result\":\"OK\""));
    }

    // --- WORLD CONFIG TESTS ---
    @Test
    void worldEnforcesMaxShieldStrength() {
        assertEquals(5, world.getMaxShieldStrength());
    }

    @Test
    void robotCreationWithValidShields() {
        Robot robot = new Robot("validBot", new Position(0, 0));
        robot.setShields(3); // Below max
        world.addRobot(robot);
        assertNotNull(world.getRobotByName("validBot"));
    }

    // --- COMBAT TESTS ---
    @Test
    void fireCommandReducesShields() {
        // Setup shooter
        Robot shooter = new Robot("shooter", new Position(0, 0));
        shooter.setDirection(Direction.EAST); // Must set direction!
        shooter.setShots(10);

        // Setup target - must be in exact line of fire
        Robot target = new Robot("target", new Position(1, 0)); // Directly east of shooter
        target.setShields(3);

        world.addRobot(shooter);
        world.addRobot(target);

        FireCommand fire = new FireCommand(world, shooter);
        String response = fire.execute();

        // Debug output
       // System.out.println("Fire response: " + response);
       // System.out.println("Target shields after: " + target.getShields());

        assertTrue(response.contains("Hit"), "Should register hit");
        assertEquals(2, target.getShields(), "Shields should decrease by 1");
    }

    @Test
    void fireCommandHitsFirstRobotInPath() {
        Robot shooter = new Robot("shooter", new Position(0, 0));
        shooter.setDirection(Direction.EAST);
        shooter.setShots(10);

        Robot firstTarget = new Robot("target1", new Position(1, 0)); // First in path
        firstTarget.setShields(3);
        Robot secondTarget = new Robot("target2", new Position(2, 0)); // Behind first
        secondTarget.setShields(3);

        world.addRobot(shooter);
        world.addRobot(firstTarget);
        world.addRobot(secondTarget);

        FireCommand fire = new FireCommand(world, shooter);
        String response = fire.execute();

        assertTrue(response.contains("Hit"));
        assertEquals(2, firstTarget.getShields()); // First target hit
        assertEquals(3, secondTarget.getShields()); // Second target untouched
    }

    @Test
    void shieldsNeverGoNegative() {
        Robot shooter = new Robot("shooter", new Position(0, 0));
        Robot target = new Robot("target", new Position(1, 0));
        target.setShields(0); // Already at 0
        world.addRobot(shooter);
        world.addRobot(target);

        new FireCommand(world, shooter).execute();
        assertEquals(0, target.getShields()); // Stays at 0
    }

    // --- HELPER METHODS ---
    private JsonObject createLaunchRequest(String robotName, int shields, int shots) {
        JsonObject request = new JsonObject();
        request.addProperty("command", "launch");
        request.addProperty("robot", robotName);

        JsonArray args = new JsonArray();
        args.add("sniper");
        args.add(shields);
        args.add(shots);
        request.add("arguments", args);

        return request;
    }
}