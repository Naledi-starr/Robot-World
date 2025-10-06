package za.co.wethinkcode.client.robots;

import za.co.wethinkcode.server.model.Direction;

/**
 * Factory class for creating {@link Robot} instances with default configurations.
 * Also provides a list of predefined robot makes.
 */
public class RobotMaker {

    /**
     * Creates a new {@link Robot} with the specified name and make.
     * The robot is initialized at position (0, 0) and faces {@code NORTH}.
     *
     * @param name the name of the robot
     * @param make the make or model of the robot
     * @return a new robot instance
     */

    public Robot createRobot(String name, String make) {
        return new Robot(name, make, 0, 0, Direction.NORTH);
    }

    /**
     * Retrieves a list of standard robot makes available for selection.
     *
     * @return an array of robot make names
     */

    public String[] getStandardMakes() {
        return new String[] {
                "Sniper",
                "Defender",
                "Trunk",
                "Razor"
        };
    }
}
