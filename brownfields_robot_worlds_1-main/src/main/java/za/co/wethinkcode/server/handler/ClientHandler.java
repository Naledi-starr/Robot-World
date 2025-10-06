package za.co.wethinkcode.server.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import za.co.wethinkcode.protocol.server.ServerCommandProcessor;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;

/**
 * Handles communication with a single client in a separate thread.
 * Listens for commands from the client, processes them, and sends back responses.
 */
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final World world;
    private final ServerCommandProcessor commandProcessor;

    /**
     * Constructs a new {@code ClientHandler} to manage communication for a connected client.
     *
     * @param socket the client socket
     * @param world the shared game world
     */
    public ClientHandler(Socket socket, World world) {
        this.clientSocket = socket;
        this.world = world;
        this.commandProcessor = new ServerCommandProcessor(world);
    }

    /**
     * Starts the handler thread.
     * Reads client input, processes commands via {@code ServerCommandProcessor},
     * and writes responses until the client disconnects or an error occurs.
     */
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            processClientCommands(in, out);

        } catch (IOException e) {
            System.out.println("Exception in client handler: " + e.getMessage());
        } finally {
            closeClientSocket();
        }
    }

    private void processClientCommands(BufferedReader in, PrintWriter out) throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String response = commandProcessor.processMessage(inputLine);
            out.println(response);
        }
    }

    private void closeClientSocket() {
        try {
            /*
                loop through list of robots and remove
             */
            List<Robot> robots = world.getRobots();
            for (Robot robot : robots) {
                String name = robot.getName();
                if (robot.getName().equals(name)) {
                    world.removeRobot(robot);
                    return;
                }
            }

            clientSocket.close();
            System.out.println("Client disconnected");
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}