package za.co.wethinkcode.WebServer.WebApiLayer;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.wethinkcode.database.DbConfig;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

import java.util.Properties;

public class WebServer {
    private final Javalin server;
    private static World world;
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private WebApiHandler webApiHandler;

    public WebServer() {
        webApiHandler = new WebApiHandler(world);
        server = Javalin.create(config -> {
                    // Enable Dev Logging for better visibility
//                    config.enableDevLogging();
                })
                .before(ctx -> {
                    // Set default content type if not set
                    if (ctx.contentType() == null) {
                        ctx.contentType("application/json");
                    }
                })
                .after(ctx -> {
                    // Log the request method and path
                    logger.info("Request: {} {}", ctx.method(), ctx.url());
                });

        // Define routes with HTTP methods
        this.server.get("/world/{world}", context -> webApiHandler.getSpecificWorld(context));
        this.server.get("/world", context -> webApiHandler.getCurrentWorld(context, world));
        this.server.post("/robot/{name}", context -> webApiHandler.create(context));
        this.server.post("/robot/{name}/{command}", context -> webApiHandler.processRobotCommands(context));
        this.server.post("/robot/{name}/{command}/{argument}", context -> webApiHandler.processRobotCommands(context));
    }

    public static void main(String[] args) {
        // Initialize the default world
        world = createDefaultWorld(20);

        // Initialize the database
        DbConfig.main(new String[]{});

        // Start the server on port 5007
        WebServer server = new WebServer();
        server.start(5007);
    }

    public void start(int port) {
        this.server.start(port);
    }

    public void stop() {
        this.server.stop();
    }

    private static World createDefaultWorld(int worldSize){
        Properties props = new Properties();

        props.setProperty("WORLD_WIDTH", String.valueOf(worldSize));
        props.setProperty("WORLD_HEIGHT", String.valueOf(worldSize));
        props.setProperty("NUM_PITS", "0");
        props.setProperty("NUM_MOUNTAINS", "0");
        props.setProperty("NUM_LAKES", "0");
        props.setProperty("VISIBILITY_RANGE", "5");
        props.setProperty("MAX_SHIELD_STRENGTH", "5");
        props.setProperty("REPAIR_TIME", "5");
        props.setProperty("RELOAD_TIME", "5");
        props.setProperty("MAX_SHOTS", "5");

        logger.info("Set WORLD_WIDTH=" + worldSize + ", WORLD_HEIGHT=" + worldSize +
                ", NUM_PITS=0, NUM_LAKES=0, NUM_MOUNTAINS=0");

        WorldConfig config = new WorldConfig(props);
        return new World(config);
    }
}
