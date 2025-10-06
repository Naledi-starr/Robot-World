package za.co.wethinkcode.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import za.co.wethinkcode.client.commands.ReloadCommand;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.world.World;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import za.co.wethinkcode.server.world.WorldConfig;

import java.util.concurrent.TimeUnit;

class ReloadCommandTest {
    private World world;
    private Robot testRobot;
    private ReloadCommand reloadCommand;

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
        world = createTestWorld();//use the world we set up
        testRobot = new Robot("testBot", new Position(0, 0));
        testRobot.setShots(2);
        reloadCommand = new ReloadCommand(world, testRobot);
    }

    @Test
    void getNameShouldReturnReload() {
        assertEquals("reload", reloadCommand.getName());
    }

    @Test
    void displayShouldReturnCorrectString() {
        assertEquals("Reload weapons", reloadCommand.display());
    }

    @Test
    void executeShouldSetRobotToReloadStatus() {
        reloadCommand.execute();
        assertEquals(Robot.Status.RELOAD, testRobot.getStatus());
    }

    @Test
    void executeShouldReturnSuccessResponse() {
        String response = reloadCommand.execute();
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        assertEquals("OK", jsonResponse.get("result").getAsString());
        assertTrue(jsonResponse.has("data"));
        assertTrue(jsonResponse.has("state"));

        JsonObject data = jsonResponse.getAsJsonObject("data");
        assertEquals("Reloading weapons", data.get("message").getAsString());
        assertEquals(5, data.get("reloadTime").getAsInt());
    }

    @Test
    void executeWhenRobotIsReloadingShouldReturnError() {
        // First execution - should succeed
        reloadCommand.execute();

        // Second execution - should fail
        String response = reloadCommand.execute();
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        assertEquals("ERROR", jsonResponse.get("result").getAsString());
        assertEquals("Robot is currently busy and cannot reload",
                jsonResponse.get("message").getAsString());
    }

    @Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    void executeShouldResetShotsAfterReloadTime() throws InterruptedException {
        reloadCommand.execute();

        // Wait for reload to complete (2 seconds + buffer)
        Thread.sleep(2500);

        assertEquals(2, testRobot.getShots());
        assertEquals(Robot.Status.RELOAD, testRobot.getStatus());
    }

    @Test
    void createSuccessResponseShouldContainCorrectData() {
        String response = reloadCommand.createSuccessResponse(2);
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        assertEquals("OK", jsonResponse.get("result").getAsString());

        JsonObject data = jsonResponse.getAsJsonObject("data");
        assertEquals("Reloading weapons", data.get("message").getAsString());
        assertEquals(2, data.get("reloadTime").getAsInt());
    }

    @Test
    void createErrorResponseShouldContainCorrectMessage() {
        String errorMessage = "Test error message";
        String response = reloadCommand.createErrorResponse(errorMessage);
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        assertEquals("ERROR", jsonResponse.get("result").getAsString());
        assertEquals(errorMessage, jsonResponse.get("message").getAsString());
    }

    @Test
    void executeShouldHandleMultipleThreadsSafely() throws InterruptedException {
        // Create multiple threads to test thread safety
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                String response = reloadCommand.execute();
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

                // Only one thread should get OK response, others should get error
                if (jsonResponse.get("result").getAsString().equals("OK")) {
                    assertEquals(Robot.Status.RELOAD, testRobot.getStatus());
                } else {
                    assertEquals("Robot is currently busy and cannot reload",
                            jsonResponse.get("message").getAsString());
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // After all threads, status should be RELOAD (not NORMAL yet)
        assertEquals(Robot.Status.RELOAD, testRobot.getStatus());
    }
}