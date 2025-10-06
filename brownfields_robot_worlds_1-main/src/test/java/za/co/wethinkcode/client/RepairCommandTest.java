package za.co.wethinkcode.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import za.co.wethinkcode.client.commands.RepairCommand;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepairCommandTest {
    private World world;
    private Robot testRobot;
    private RepairCommand repairCommand;


    private World createTestWorld() {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH", "100");
            properties.setProperty("WORLD_HEIGHT", "100");
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
    void setUp() {
        world = createTestWorld();
        //when(world.getRepairTime()).thenReturn(2);
        //when(world.getMaxShieldStrength()).thenReturn(5);

        testRobot = new Robot("testBot", new Position(0, 0));
        testRobot.setShields(2);

        repairCommand = new RepairCommand(world, testRobot);
    }

    @Test
    void getNameShouldReturnRepair() {
        assertEquals("repair", repairCommand.getName());
    }

    @Test
    void displayShouldReturnCorrectString() {
        assertEquals("Instruct the robot to repair its shields", repairCommand.display());
    }

    @Test
    void executeShouldSetRobotToRepairStatus() {
        repairCommand.execute();
        assertEquals(Robot.Status.REPAIR, testRobot.getStatus());
    }

    @Test
    void executeShouldReturnSuccessResponse() {
        String response = repairCommand.execute();
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        assertEquals("OK", jsonResponse.get("result").getAsString());
        assertTrue(jsonResponse.has("data"));
        assertTrue(jsonResponse.has("state"));

        JsonObject data = jsonResponse.getAsJsonObject("data");
        assertEquals("Done", data.get("message").getAsString());
    }

    @Test
    void executeWhenRobotIsRepairingShouldReturnError() {
        repairCommand.execute(); // Start repair
        String response = repairCommand.execute(); // Try again while repairing
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        assertEquals("ERROR", jsonResponse.get("result").getAsString());
        assertEquals("Robot is currently busy and cannot repair", jsonResponse.get("message").getAsString());
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void executeShouldResetShieldsAfterRepairTime() throws InterruptedException {
        repairCommand.execute();
        Thread.sleep(2500); // Wait a little longer than repair time

        assertEquals(2, testRobot.getShields());
        assertEquals(Robot.Status.REPAIR, testRobot.getStatus());
    }

    @Test
    void executeShouldHandleMultipleThreadsSafely() throws InterruptedException {
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                String response = repairCommand.execute();
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                if (jsonResponse.get("result").getAsString().equals("OK")) {
                    assertEquals(Robot.Status.REPAIR, testRobot.getStatus());
                } else {
                    assertEquals("Robot is currently busy and cannot repair", jsonResponse.get("message").getAsString());
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        assertEquals(Robot.Status.REPAIR, testRobot.getStatus());
    }
}
