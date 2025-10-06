package za.co.wethinkcode.WebServer.WebApiLayer;

import za.co.wethinkcode.WebServer.DataAccessLayer.*;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import za.co.wethinkcode.WebServer.DomainLayer.WorldApi;
import za.co.wethinkcode.protocol.server.ServerCommandProcessor;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.persistence.WorldDao;
import za.co.wethinkcode.server.world.World;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Scanner;

public class WebApiHandler {
    private static WorldDB database;
    private static ServerCommandProcessor commandProcessor = null;

    public WebApiHandler(World world){
        commandProcessor = new ServerCommandProcessor(world);
        try {
            database = new TestDatabase();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get current world
     *
     * @param context The Javalin Context for the HTTP GET Request
     */
    public static void getCurrentWorld(Context context, World world) {
        context.json(world.displayJson());
    }

    /**
     * Get specific world
     *
     * @param context The Javalin Context for the HTTP GET Request
     */
    public static void getSpecificWorld(Context context) throws SQLException {
        String worldName = context.pathParamAsClass("world", String.class).get();
        WorldDao worldDataObject = new WorldDao("jdbc:sqlite:robot-worlds.db");
        World world = worldDataObject.restoreWorldByName(worldName);
        if (world == null) {
            throw new NotFoundResponse("World not found: " + worldName);
        }
        context.json(world.displayJson());
    }

    public static void processRobotCommands(Context context){
        String robotName = context.pathParamAsClass("name", String.class).get();
        String command = context.pathParamAsClass("command", String.class).get();
        try{
            int argument = context.pathParamAsClass("argument", Integer.class).get();
            String json = "{\"robot\":\"%s\",\"command\":\"%s\",\"arguments\":[\"%d\",5,10]}".formatted(robotName, command, argument);
            String response = commandProcessor.processMessage(json);
            context.header("Location", "robot/"+robotName);
            context.status(HttpStatus.CREATED);
            context.json(response);
        }catch (RuntimeException e){
            String json = "{\"robot\":\"%s\",\"command\":\"%s\",\"arguments\":[\"Sniper\",5,10]}".formatted(robotName, command);
            String response = commandProcessor.processMessage(json);
            context.header("Location", "robot/"+robotName);
            context.status(HttpStatus.CREATED);
            context.json(response);
        }
    }


    /**
     * Create a new world
     *
     * @param context The Javalin Context for the HTTP POST Request
     */
    public static void create(Context context) throws IOException {
        String robotName = context.pathParamAsClass("name", String.class).get();
        String json = "{\"robot\":\"%s\",\"command\":\"launch\",\"arguments\":[\"Sniper\",5,10]}".formatted(robotName);
        String response = commandProcessor.processMessage(json);
        context.header("Location", "robot/"+robotName);
        context.status(HttpStatus.CREATED);
        context.json(response);
    }


}

