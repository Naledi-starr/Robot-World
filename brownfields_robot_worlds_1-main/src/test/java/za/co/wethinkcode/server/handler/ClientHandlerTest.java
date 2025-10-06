//package za.co.wethinkcode.server.handler;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import za.co.wethinkcode.robots.server.RobotWorldClient;
//import za.co.wethinkcode.robots.server.RobotWorldJsonClient;
//import za.co.wethinkcode.server.model.Direction;
//import za.co.wethinkcode.server.model.Position;
//import za.co.wethinkcode.server.model.Robot;
//import za.co.wethinkcode.server.world.World;
//import za.co.wethinkcode.server.world.WorldConfig;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.fail;
//
//public class ClientHandlerTest {
//
//    private Robot robot;
//    private World world;
//    private ByteArrayOutputStream serverOut;
//    private final static int DEFAULT_PORT = 5000;
//    private final static String DEFAULT_IP = "localhost";
//    private final RobotWorldClient serverClient = new RobotWorldJsonClient();
//
//    @BeforeEach
//    void connectToServer(){
//        serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
//    }
//
//    private World createTestWorld(int width, int height) {
//        WorldConfig config = new WorldConfig() {{
//            properties.setProperty("WORLD_WIDTH", String.valueOf(width));
//            properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
//            properties.setProperty("NUM_PITS", "0");
//            properties.setProperty("NUM_MOUNTAINS", "0");
//            properties.setProperty("NUM_LAKES", "0");
//            properties.setProperty("VISIBILITY_RANGE", "5");
//            properties.setProperty("MAX_SHIELD_STRENGTH", "5");
//            properties.setProperty("REPAIR_TIME", "5");
//            properties.setProperty("RELOAD_TIME", "5");
//        }};
//        return new World(config);
//    }
//
//    @BeforeEach
//    void setUp() {
//        world = createTestWorld(10, 10); // initialize mock world
//        serverOut = new ByteArrayOutputStream();
//
//        robot = new Robot("Trunk", new Position(0, 0));
//        robot.setMake("Trunk");
//        robot.setDirection(Direction.NORTH);
//        robot.setShots(3);
//        world.addRobot(robot);
//    }
//
//    @Test
//    void testRobotExitWorld() throws IOException {
//        ServerSocket serverSocket = new ServerSocket(0); // bind to any free port
//        int port = serverSocket.getLocalPort();
//
//        // client simulation thread
//        new Thread(() -> {
//            try (Socket client = new Socket("localhost", port)) {
//                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
//                out.println("{\"robot\":\"Trunk\",\"command\":\"exit\"}");
//
//            } catch (IOException e) {
//                fail("Client exit failed");
//                // throw new RuntimeException(e);
//            }
//        }).start();
//
//        // wait for connection
//        Socket serverSideSocket = new Socket("localhost", port);
//        ClientHandler handler = new ClientHandler(serverSideSocket, world);
//        handler.run();
//
//        List<Robot> remaining = world.getRobots();
//        boolean existing = remaining.stream().anyMatch(robot -> robot.getMake().equals("Trunk"));
//        assertFalse(existing);
//
//        serverSocket.close();
//    }
//}