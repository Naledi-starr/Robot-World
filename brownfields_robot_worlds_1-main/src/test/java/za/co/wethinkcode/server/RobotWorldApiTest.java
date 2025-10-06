package za.co.wethinkcode.server;

//import kong.unirest.HttpResponse;
//import kong.unirest.JsonNode;
//import kong.unirest.Unirest;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class RobotWorldApiTest {
//    private static RobotWorldApiServer server;
//
//    @BeforeAll
//    public static void startServer() throws SQLException {
//        // Set up in-memory database for testing
//        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute("CREATE TABLE world_properties (property_name TEXT PRIMARY KEY, value INTEGER)");
//            stmt.execute("CREATE TABLE obstacles (type TEXT, x INTEGER, y INTEGER, width INTEGER, height INTEGER)");
//            stmt.execute("INSERT INTO world_properties (property_name, value) VALUES ('WORLD_WIDTH', 200)");
//            stmt.execute("INSERT INTO world_properties (property_name, value) VALUES ('WORLD_HEIGHT', 150)");
//            stmt.execute("INSERT INTO obstacles (type, x, y, width, height) VALUES ('mountain', 10, 10, 5, 5)");
//            stmt.execute("INSERT INTO obstacles (type, x, y, width, height) VALUES ('lake', 20, 20, 3, 3)");
//        }
//        server = new RobotWorldApiServer();
//        server.start(7000);
//    }
//
//    @AfterAll
//    public static void stopServer() {
//        server.stop();
//    }
//
//    @Test
//    @DisplayName("GET /world")
//    public void getCurrentWorld() {
//        HttpResponse<JsonNode> response = Unirest.get("http://localhost:7000/world").asJson();
//        assertEquals(200, response.getStatus());
//        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));
//
//        JsonNode json = response.getBody();
//        assertEquals(200, json.getObject().getInt("width"));
//        assertEquals(150, json.getObject().getInt("height"));
//        assertEquals(2, json.getObject().getJSONArray("obstacles").length());
//        assertEquals(0, json.getObject().getJSONArray("robots").length());
//    }
//
//    @Test
//    @DisplayName("POST /robot/{name}")
//    public void launchRobot() {
//        HttpResponse<JsonNode> response = Unirest.post("http://localhost:7000/robot/testRobot")
//                .header("Content-Type", "application/json")
//                .body("{\"command\":\"launch\",\"arguments\":[\"shooter\",\"5\",\"5\"]}")
//                .asJson();
//        assertEquals(201, response.getStatus());
//        assertEquals("/robot/testRobot", response.getHeaders().getFirst("Location"));
//        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));
//
//        JsonNode json = response.getBody();
//        assertEquals("OK", json.getObject().getString("result"));
//        assertEquals("Robot launched successfully", json.getObject().getJSONObject("data").getString("message"));
//        assertEquals("shooter", json.getObject().getJSONObject("state").getString("make"));
//        assertEquals(5, json.getObject().getJSONObject("state").getInt("shields"));
//        assertEquals(5, json.getObject().getJSONObject("state").getInt("shots"));
//        assertEquals("NORMAL", json.getObject().getJSONObject("state").getString("status"));
//        assertEquals(0, json.getObject().getJSONObject("state").getJSONArray("position").getInt(0));
//        assertEquals(0, json.getObject().getJSONObject("state").getJSONArray("position").getInt(1));
//        assertEquals("NORTH", json.getObject().getJSONObject("state").getString("direction"));
//    }
//
//    @Test
//    @DisplayName("POST /robot/{name} with invalid command")
//    public void invalidCommand() {
//        HttpResponse<JsonNode> response = Unirest.post("http://localhost:7000/robot/testRobot")
//                .header("Content-Type", "application/json")
//                .body("{\"command\":\"invalid\",\"arguments\":[]}")
//                .asJson();
//        assertEquals(400, response.getStatus());
//        assertEquals("ERROR", response.getBody().getObject().getString("result"));
//        assertTrue(response.getBody().getObject().getString("message").contains("Only launch command is supported"));
//    }
//
//    @Test
//    @DisplayName("POST /robot/{name} with invalid arguments")
//    public void invalidArguments() {
//        HttpResponse<JsonNode> response = Unirest.post("http://localhost:7000/robot/testRobot")
//                .header("Content-Type", "application/json")
//                .body("{\"command\":\"launch\",\"arguments\":[\"shooter\"]}")
//                .asJson();
//        assertEquals(400, response.getStatus());
//        assertEquals("ERROR", response.getBody().getObject().getString("result"));
//        assertTrue(response.getBody().getObject().getString("message").contains("Launch command requires [make, maxShields, maxShots]"));
//    }
}