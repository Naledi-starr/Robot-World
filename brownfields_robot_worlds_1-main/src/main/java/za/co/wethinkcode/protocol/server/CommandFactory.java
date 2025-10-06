package za.co.wethinkcode.protocol.server;

import za.co.wethinkcode.client.commands.*;
import za.co.wethinkcode.server.commands.*;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.model.Robot;

/**
 * Factory class for creating various Command instances that control robot behavior in the game world.
 * Each command interacts with the current game environment represented by the World object.
 */
public record CommandFactory(World world) {

    /**
     * Creates a command that dumps the current state of the world.
     *
     * @return a DumpCommand instance
     */
    public Command createDumpCommand() {
        return new DumpCommand(world);
    }

    public Command createSaveCommand() {
        return new SaveCommand(world);
    }


    /**
     * Creates a command that retrieves a list of all active robots.
     *
     * @return a RobotsCommand instance
     */
    public Command createRobotsCommand() {
        return new RobotsCommand(world);
    }

    /**
     * Creates a command that ends the game session for a robot.
     *
     * @return a QuitCommand instance
     */
    public Command createQuitCommand() {
        return new QuitCommand(world);
    }

    /**
     * Creates a command that moves a robot forward by a specified number of steps.
     *
     * @param robot the Robot to move
     * @param steps number of steps to move forward
     * @return a ForwardCommand instance
     */
    public Command createForwardCommand(Robot robot, int steps) {
        return new ForwardCommand(world, robot, steps);
    }

    /**
     * Creates a command that moves a robot backward by a specified number of steps.
     *
     * @param robot the Robot to move
     * @param steps number of steps to move backward
     * @return a BackCommand instance
     */
    public Command createBackCommand(Robot robot, int steps) {
        return new BackCommand(world, robot, steps);
    }

    /**
     * Creates a command that turns a robot to the left.
     *
     * @param robot the Robot to turn
     * @return a TurnLeftCommand instance
     */
    public Command createTurnLeftCommand(Robot robot) {
        return new TurnLeftCommand(world, robot);
    }

    /**
     * Creates a command that turns a robot to the right.
     *
     * @param robot the Robot to turn
     * @return a TurnRightCommand instance
     */
    public Command createTurnRightCommand(Robot robot) {
        return new TurnRightCommand(world, robot);
    }

    /**
     * Creates a command that lets a robot fire its weapon.
     *
     * @param robot the Robot that will fire
     * @return a FireCommand instance
     */
    public Command createFireCommand(Robot robot) {
        return new FireCommand(world, robot);
    }

    /**
     * Creates a command that reloads a robot's weapon.
     *
     * @param robot the Robot to reload
     * @return a ReloadCommand instance
     */
    public Command createReloadCommand(Robot robot) {
        return new ReloadCommand(world, robot);
    }

    /**
     * Creates a command that repairs a robot.
     *
     * @param robot the Robot to repair
     * @return a RepairCommand instance
     */
    public Command createRepairCommand(Robot robot) {
        return new RepairCommand(world, robot);
    }

}
