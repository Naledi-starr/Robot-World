package za.co.wethinkcode.WebServer.WebApiLayer;

import io.javalin.Javalin;

/**
 * A Web API server for the RobotWorld application, providing an HTTP interface
 * to interact with the game world. It handles requests to retrieve world details
 * and manage robots using Javalin as the web framework.
 */
public class RobotWorldApiServer {
    private final Javalin server;

    /**
     * Constructs a new RobotWorldApiServer, initializing the Javalin server
     * and configuring HTTP endpoints for world retrieval and robot commands.
     * Sets the default Content-Type to application/json for all responses.
     */
    public RobotWorldApiServer() {
        server = Javalin.create();

        // Set the default Content-Type to application/json for all responses
        server.before(ctx -> ctx.header("Content-Type", "application/json"));

        server.get("/world", RobotWorldApiHandler::getWorld);
        server.get("/world/{name}", RobotWorldApiHandler::getWorld);
        server.post("/robot/{name}", RobotWorldApiHandler::handleCommand);
    }

    /**
     * Starts the Javalin server on the specified port, enabling the Web API
     * to accept incoming HTTP requests.
     *
     * @param port the port number to listen on (e.g., 7000)
     */
    public void start(int port) {
        server.start(port);
    }

    /**
     * Stops the Javalin server, shutting down the Web API and releasing resources.
     */
    public void stop() {
        server.stop();
    }

    /**
     * A Main entry point to run the RobotWorld Web API server standalone.
     * Creates an instance of RobotWorldApiServer and starts it on port 7000,
     * avoiding conflict with the socket-based RobotWorldServer.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        RobotWorldApiServer api = new RobotWorldApiServer();
        api.start(7000);
    }

}