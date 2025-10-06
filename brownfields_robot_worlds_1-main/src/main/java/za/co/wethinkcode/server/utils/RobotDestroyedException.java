package za.co.wethinkcode.server.utils;

/**
 * Custom exception thrown when a robot is destroyed,
 * such as by falling into a pit or running out of shields.
 */
public class RobotDestroyedException extends RuntimeException {
    public RobotDestroyedException(String message) {
        super(message);
    }
}