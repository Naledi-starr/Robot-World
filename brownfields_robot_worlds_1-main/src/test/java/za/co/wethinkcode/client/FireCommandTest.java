package za.co.wethinkcode.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.commands.FireCommand;
import za.co.wethinkcode.client.commands.ForwardCommand;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static za.co.wethinkcode.server.model.Direction.NORTH;

public class FireCommandTest {

    private World world;
    private Robot shooter;

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
        shooter = new Robot("Shooter", new Position(0, 0));
        shooter.setMake("Sniper");
        shooter.setDirection(NORTH);
        shooter.setShots(3);
        world.addRobot(shooter);
    }

    @Test
    public void testShotDistanceCalculation() {
        shooter.setShots(1);

        FireCommand fireCommand = new FireCommand(world, shooter);
        fireCommand.execute();

        // After firing once, remaining shots should be 0
        assertEquals(0, shooter.getShots());
    }

    @Test
    public void testNoShotsAvailable() {
        shooter.setShots(0);  // Set shooter shots to 0
        FireCommand fireCommand = new FireCommand(world, shooter);

        String result = fireCommand.execute();  // Execute fire command and get result

        JsonObject response = JsonParser.parseString(result).getAsJsonObject();
        // Check the response when no shots are available
        assertEquals("ERROR", response.get("result").getAsString());
        assertEquals("No shots available", response.get("message").getAsString());
    }

    @Test
    void testFireMiss() {
        // Only shooter in world (no target)
        FireCommand fireCommand = new FireCommand(world, shooter);
        String result = fireCommand.execute();

        JsonObject response = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("OK", response.get("result").getAsString(), "Should return OK for valid fire command");
        assertEquals("Miss", response.getAsJsonObject("data").get("message").getAsString());
        assertEquals(2, response.getAsJsonObject("state").get("shots").getAsInt(), "Shots should be decremented");
    }

    @Test
    public void testFireAtMaxVisibilityRange() {
        Robot rangeShooter = new Robot("RangeShooter", new Position(0, 0));
        Robot target = new Robot("Target", new Position(0, 5));
        rangeShooter.setShots(1); //According to the range formula, max-range(5 units) is at 1 shot
        target.setShields(5);
        world.addRobot((target));
        world.addRobot(rangeShooter);

        FireCommand fireCommand = new FireCommand(world, rangeShooter);
        String result = fireCommand.execute();

        JsonObject response = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("OK", response.get("result").getAsString());
        assertEquals("Hit", response.getAsJsonObject("data").get("message").getAsString());
    }

    @Test
    public void testFireHitRobot() {
        // Create a target robot to simulate hit
        Robot target = new Robot("Target", new Position(0, 1));
        target.setMake("Dummy");
        target.setDirection(Direction.SOUTH);
        target.setShields(1);
        world.addRobot(target);


        FireCommand fireCommand = new FireCommand(world, shooter);
        String result = fireCommand.execute();  // Execute fire command

        JsonObject response = JsonParser.parseString(result).getAsJsonObject();
        // If the FireCommand is set to avoid hits for now, check for "Miss" instead of "Hit"
        assertEquals("OK", response.get("result").getAsString());
        assertEquals("Hit", response.getAsJsonObject("data").get("message").getAsString());
        assertEquals(2, response.getAsJsonObject("state").get("shots").getAsInt());
    }

    @Test
    public void testFireDestroysRobot(){
        Robot destroyer = new Robot("Destroyer", new Position(0, 2));
        Robot target = new Robot("Target", new Position(0, 3));
        target.setShields(0);
        destroyer.setShots(2);
        world.addRobot(target);
        world.addRobot(destroyer);

        FireCommand fireCommand = new FireCommand(world, destroyer);
        String result  = fireCommand.execute();

        JsonObject response = JsonParser.parseString(result).getAsJsonObject();

        assertEquals("OK", response.get("result").getAsString(), "Should return OK for valid fire command");
        assertEquals("Hit", response.getAsJsonObject("data").get("message").getAsString());
        assertEquals("DEAD", target.getStatus().toString());
        //assertFalse(world.getRobots().contains(target), "Target should be moved from world after being destroyed ");
    }

    @Test
    public void testFireHitRobotFacingEast(){
        shooter.setDirection(Direction.EAST);
        Robot target = new Robot("Target", new Position(3, 0));
        target.setMake("Dummy");
        target.setShields(0);
        world.addRobot(target);

        FireCommand fireCommand= new FireCommand(world, shooter);
        String result = fireCommand.execute();

        JsonObject response = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("OK", response.get("result").getAsString());
        assertEquals("Hit", response.getAsJsonObject("data").get("message").getAsString());
    }


    @Test
    public void testShotBlockedByObstacle() {
        Mountain mountain = new Mountain(0, 5, 5,5);
        world.addObstacle(mountain);

        Robot shooter = new Robot("Shooter", new Position(3, 4));
        shooter.setShots(1);
        world.addRobot(shooter);


        //Override FireCommand to simulate blocked shot by the target
        FireCommand fireCommand = new FireCommand(world, shooter) ;
        String result = fireCommand.execute();

        JsonObject response = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("OK", response.get("result").getAsString());
        assertEquals("Hit Obstacle", response.getAsJsonObject("data").get("message").getAsString());
    }

    @Test
    public void testFireBlockedByAnotherRobot(){
        Robot blocker = new Robot("Blocker", new Position(0, 2));
        Robot target = new Robot("Target", new Position(0, 1));
        blocker.setShields(5);
        target.setShields(5);
        target.setShots(3);

        world.addRobot(blocker);
        world.addRobot(target);

        FireCommand fireCommand = new FireCommand(world, target);
        fireCommand.execute();

        assertEquals(4, blocker.getShields(), "Blocker should have 1 shield reduced");
        assertEquals(5, target.getShields(), "Target should remain unharmed");
    }

    @Test
    public void testShotsDecrementedAfterFiring() {
        int initialShots = shooter.getShots();
        FireCommand fireCommand = new FireCommand(world, shooter);
        fireCommand.execute();

        // Verify that the shots have been decremented by 1
        assertEquals(initialShots - 1, shooter.getShots());
    }

    @Test
    public void testsFireReducesSheilds(){
        Robot target = new Robot("Target", new Position(0, 3));
        target.setShields(3);
        world.addRobot(target);

        FireCommand fireCommand = new FireCommand(world, shooter);
        fireCommand.execute();

        assertEquals(2, target.getShields(), "Target sheields should be reduced by 1");
    }

    @Test
    public void testRobotSuccessfullyUsesFireWeapon() {
        Robot shooter = new Robot("Shooter", new Position(2, 4));
        Robot target = new Robot("Target", new Position(2, 5));
        shooter.setDirection(Direction.NORTH);
        shooter.setShots(3);
        target.setShields(2);

        world.addRobot(shooter);
        world.addRobot(target);

        FireCommand fireCommand = new FireCommand(world, shooter);
        String result = fireCommand.execute();
        JsonObject response = JsonParser.parseString(result).getAsJsonObject();

        assertEquals("OK", response.get("result").getAsString(), "Fire command should be executed successfully");
        assertEquals("Hit", response.getAsJsonObject("data").get("message").getAsString(), "Target should be hit.");
        assertEquals(2, shooter.getShots(), "Shooter should have 1 less shot.");
        assertEquals(1, target.getShields(), "Target's shields should be reduced by 1.");
    }

//    @Test
//    public void testFireHitsObstacle() {
//        // Place the shooter at (0,0) facing NORTH
//        shooter.setPosition(new Position(0, 0));
//        shooter.setDirection(Direction.NORTH);
//        shooter.setShots(3);
//
//        Obstacle mountain = new Mountain(new Position(0, 1));
//        world.addObstacle(mountain);
//        world.addRobot(shooter);
//
//        FireCommand fireCommand = new FireCommand(world, shooter);
//        String result = fireCommand.execute();
//
//        JsonObject response = JsonParser.parseString(result).getAsJsonObject();
//
//        assertEquals("OK", response.get("result").getAsString());
//        assertEquals("Hit Obstacle", response.getAsJsonObject("data").get("message").getAsString()); // Adjust message if needed
//    }



}
