package za.co.wethinkcode.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.robots.Robot;
import za.co.wethinkcode.server.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;


/**
 * The server for these tests is run with the following command:
 *  mvn exec:java -Dexec.mainClass="za.co.wethinkcode.server.RobotWorldServer" -Dexec.args="-p 5000 -s 5 -o 3,7"
 */
public class ServerStartCommandlineTest {
    private final mockClient client = new mockClient();

    @BeforeEach
    void connectToServer(){
        client.connect("localhost", 5000);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    @AfterEach
    void disconnectFromServer(){
        client.disconnect();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Test
    void testConnectsCommandPort() {
        assertTrue(client.isConnected());
    }

    @Test
    void canLaunchToCommandLineWorld(){
        // When I send a valid launch request to the server
        String request = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode response = client.sendRequest(request);

        // Then I should get a valid response from the server
        assertNotNull(response.get("result"));
        assertEquals("OK", response.get("result").asText());
//
        // And the position should be (x:0, y:0)
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("position"));
        assertEquals(0, response.get("data").get("position").get(0).asInt());
        assertEquals(0, response.get("data").get("position").get(1).asInt());

        // And I should also get the state of the robot
        assertNotNull(response.get("state"));
    }

    @Test
    void botMovesNoFarNorthThanEdgeWorld(){

        // When I send a valid launch request to the server
        String request = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        client.sendRequest(request);

        String moveRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"forward\"," +
                "  \"arguments\": [\"10\"]" +
                "}";

        JsonNode response = client.sendRequest(moveRequest);

        System.out.println(response);

        // Then I should get a valid response from the server
        assertNotNull(response.get("result"));
        assertEquals("OK", response.get("result").asText());


        // And the position should be (x:0, y:5)
        assertNotNull(response.get("data"));
        assertEquals(0, response.get("state").get("position").get(0).asInt());
        assertEquals(0, response.get("state").get("position").get(1).asInt());
        // And I should also get the state of the robot
        assertEquals("Obstructed",response.get("data").get("message").asText());
    }

    @Test
    void botMovesNoFarEastThanEdgeWorld(){

        // When I send a valid launch request to the server
        String request = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        client.sendRequest(request);

        String turnRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"turn\"," +
                "  \"arguments\": [\"right\"]" +
                "}";

        client.sendRequest(turnRequest);

        client.sendRequest(request);

        String moveRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"forward\"," +
                "  \"arguments\": [\"10\"]" +
                "}";

        JsonNode response = client.sendRequest(moveRequest);

        System.out.println(response);

        // Then I should get a valid response from the server
        assertNotNull(response.get("result"));
        assertEquals("OK", response.get("result").asText());


        // And the position should be (x:0, y:5)
        assertNotNull(response.get("data"));
        assertEquals(0, response.get("state").get("position").get(0).asInt());
        assertEquals(0, response.get("state").get("position").get(1).asInt());
        // And I should also get the state of the robot
        assertEquals("Obstructed",response.get("data").get("message").asText());
    }

    public static class mockClient {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private Socket socket;
        private PrintStream out;
        private BufferedReader in;

        public void connect(String ipAddress, int port) {
            try {
                socket = new Socket(ipAddress, port);
                out = new PrintStream(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
            } catch (IOException e) {
                //error connecting should just throw Runtime error and fail test
                throw new RuntimeException("Error connecting to Robot Worlds server.", e);
            }
        }

        public boolean isConnected() {
            return socket.isConnected();
        }

        public void disconnect() {
            try {
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                //error connecting should just throw Runtime error and fail test
                throw new RuntimeException("Error disconnecting from Robot Worlds server.", e);
            }
        }

        public JsonNode sendRequest(String requestJsonString) {
            try {
                out.println(requestJsonString);
                out.flush();
                return OBJECT_MAPPER.readTree(in.readLine());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing server response as JSON.", e);
            } catch (IOException e) {
                throw new RuntimeException("Error reading server response.", e);
            }
        }
    }
}
