//package za.co.wethinkcode.client;
//
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import za.co.wethinkcode.client.commands.FireCommand;
//import za.co.wethinkcode.server.world.WorldConfig;
//import za.co.wethinkcode.client.commands.BackCommand;
//import za.co.wethinkcode.client.commands.ForwardCommand;
//import za.co.wethinkcode.server.model.Direction;
//
//import za.co.wethinkcode.server.model.Position;
//import za.co.wethinkcode.server.model.Robot;
//import za.co.wethinkcode.server.world.World;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import za.co.wethinkcode.server.world.World;
//import za.co.wethinkcode.server.world.WorldConfig;
//
//private World createSmallTestWorld() {
//        WorldConfig config = new WorldConfig() {{
//            properties.setProperty("WORLD_WIDTH", "2");
//            properties.setProperty("WORLD_HEIGHT", "2");
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
//@Test
//public void testFireRemovesRobotWhenHit() {
//    // Create a 2x2 test world
//    World = createSmallTestWorld();
//
//    // Create shooter HAL at (0,0) facing NORTH
//    Robot hal = new Robot("HAL", new Position(0, 0));
//    hal.setDirection(Direction.NORTH);
//    hal.setShots(1);  // One shot is enough
//    hal.setShields(5);
//    World.addRobot(hal);
//
//    // Create target R2D2 at (0,1) with 0 shields (so it gets destroyed)
//    Robot r2d2 = new Robot("R2D2", new Position(0, 1));
//    r2d2.setShields(0);
//    r2d2.setShots(1);
//    World.addRobot(r2d2);
//
//    // Execute fire command
//    FireCommand fireCommand = new FireCommand(World, hal);
//    String result = fireCommand.execute();
//
//    JsonObject response = JsonParser.parseString(result).getAsJsonObject();
//
//    // Check the response
//    assertEquals("OK", response.get("result").getAsString());
//    assertEquals("Hit", response.getAsJsonObject("data").get("message").getAsString());
//
//    // Verify that R2D2 has been removed from the world
//    assertNull(World.getRobotByName("R2D2"), "R2D2 should be removed from the world after being hit.");
//}
//
//}
