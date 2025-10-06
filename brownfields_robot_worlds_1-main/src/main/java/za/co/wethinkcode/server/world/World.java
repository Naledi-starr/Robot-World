package za.co.wethinkcode.server.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import netscape.javascript.JSObject;
import za.co.wethinkcode.client.commands.LookCommand;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;

import java.util.*;

import za.co.wethinkcode.server.utils.MovementValidator;
import za.co.wethinkcode.server.world.obstacles.*;

/**
 * Represents the game world with a fixed size, obstacles, and robots.
 * Manages world boundaries, obstacle generation, robot tracking, and visibility.
 */
public class World {
    private final int width;
    private final int height;
    private final int visibilityRange;
    private final int maxShieldStrength;
    private final int reloadTime;
    private final int repairTime;
    private final int maxShots;
    private final List<Obstacle> obstacles;
    private final List<Robot> robots;
    private final Random random = new Random();
    private final WorldConfig config;

    public World(WorldConfig worldConfig) {
        this.config = worldConfig;
        this.width = worldConfig.getWidth();
        this.height = worldConfig.getHeight();
        this.visibilityRange = worldConfig.getVisibilityRange();
        this.maxShieldStrength = worldConfig.getMaxShieldStrength();
        this.reloadTime = worldConfig.getReloadTime();
        this.repairTime = worldConfig.getRepairTime(); // Initialize repairTime
        this.maxShots = worldConfig.getMaxShots();
        this.obstacles = new ArrayList<>();
        this.robots = new ArrayList<>();
        generateObstacles(worldConfig.getNumMountains(), worldConfig.getNumLakes(), worldConfig.getNumPits());
    }

    /**
     * Returns the repair time for robots.
     *
     * @return Repair time in ticks.
     */
    public int getRepairTime() {
        return repairTime;
    }

    private void generateObstacles(int mountains, int lakes, int pits) {//generates each obs
        Set<Position> occupiedPositions = new HashSet<>();

        // Generate mountains
        for (int i = 0; i < mountains; i++) {
            obstacles.add(createRandomObstacle("mountain", occupiedPositions));
        }

        // Generate lakes
        for (int i = 0; i < lakes; i++) {
            obstacles.add(createRandomObstacle("lake", occupiedPositions));
        }

        // Generate pits
        for (int i = 0; i < pits; i++) {
            obstacles.add(createRandomObstacle("pit", occupiedPositions));
        }
    }

    private Obstacle createRandomObstacle(String type, Set<Position> occupied) {
        int maxAttempts = 100;
        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int sizeX = 1 + random.nextInt(3);
            int sizeY = 1 + random.nextInt(3);

            if (!isWithinBounds(x, y, sizeX, sizeY)) {
                continue;
            }

            if (!arePositionsAvailable(x, y, sizeX, sizeY, occupied)) {
                continue;
            }

            markPositionsOccupied(x, y, sizeX, sizeY, occupied);
            return createObstacle(type, x, y, sizeX, sizeY);
        }
        return null;
    }

    private boolean isWithinBounds(int x, int y, int sizeX, int sizeY) {
        return x + sizeX <= width && y + sizeY <= height;
    }

    private boolean arePositionsAvailable(int x, int y, int sizeX, int sizeY, Set<Position> occupied) {
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                if (occupied.contains(new Position(x + dx, y + dy))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void markPositionsOccupied(int x, int y, int sizeX, int sizeY, Set<Position> occupied) {
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                occupied.add(new Position(x + dx, y + dy));
            }
        }
    }

    private Obstacle createObstacle(String type, int x, int y, int sizeX, int sizeY) {
        switch (type) {
            case "mountain":
                return new Mountain(x, y, sizeX, sizeY);
            case "lake":
                return new Lake(x, y, sizeX, sizeY);
            case "pit":
                return new Pit(x, y, sizeX, sizeY);
                case "mine":
                    return new Mine(x, y, sizeX, sizeY);
            default:
                return null;
        }
    }

    /**
     * Checks if a given position is within the world's boundaries.
     *
     * @param position The position to check.
     * @return True if the position is inside the world bounds; false otherwise.
     */
    public boolean isPositionValid(Position position) {
        int x = position.getX();
        int y = position.getY();
        return x >= 0 && x < width &&   // 0 to width-1
                y >= 0 && y < height;    // 0 to height-1
    }

    /**
     * Checks if a position is blocked by any obstacle.
     *
     * @param position The position to check.
     * @return True if blocked by an obstacle; false otherwise.
     */
    public boolean isPositionBlocked(Position position) {//
        for (Obstacle obstacle : obstacles) {
            if (obstacle.blocksPosition(position.getX(), position.getY())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of obstacles visible to a given robot based on its position and direction.
     *
     * @param robot The robot whose visibility is checked.
     * @return List of visible obstacles.
     */
    public List<Obstacle> getVisibleObstacles(Robot robot) {
        List<Obstacle> visible = new ArrayList<>();
        Position robotPos = robot.getPosition();

        for (Obstacle obstacle : obstacles) {
            if (isObstacleVisible(obstacle, robotPos, robot.getDirection())) {
                visible.add(obstacle);
            }
        }
        return visible;
    }

    private boolean isObstacleVisible(Obstacle obstacle, Position robotPos, Direction facing) {
        int dx = obstacle.getX() - robotPos.getX();
        int dy = obstacle.getY() - robotPos.getY();

        // Calculate Manhattan distance
        int distance = Math.abs(dx) + Math.abs(dy);
        if (distance > visibilityRange) return false;

        // Check if in field of view (simplified)
        switch (facing) {
            case NORTH:
                return dy >= 0 && Math.abs(dx) <= dy;
            case EAST:
                return dx >= 0 && Math.abs(dy) <= dx;
            case SOUTH:
                return dy <= 0 && Math.abs(dx) <= -dy;
            case WEST:
                return dx <= 0 && Math.abs(dy) <= -dx;
            default:
                return false;
        }
    }

    /**
     * Adds a robot to the world.
     *
     * @param robot The robot to add.
     */
    public synchronized void addRobot(Robot robot) {
        robots.add(robot);
    }

    /**
     * Removes a robot from the world.
     *
     * @param robot The robot to remove.
     */
    public synchronized void removeRobot(Robot robot) {
        robots.remove(robot);
    }

    /**
     * Returns a copy of the list of robots currently in the world.
     *
     * @return List of robots.
     */
    public synchronized List<Robot> getRobots() {
        return new ArrayList<>(robots);
    }

    /**
     * Finds and returns a robot by name (case-insensitive).
     *
     * @param name The name of the robot.
     * @return The robot if found; null otherwise.
     */
    public synchronized Robot getRobotByName(String name) {
        for (Robot robot : robots) {
            if (robot.getName().equalsIgnoreCase(name)) {
                return robot;
            }
        }
        return null;
    }

    /**
     * Returns the world's width.
     *
     * @return Width in units.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the world's height.
     *
     * @return Height in units.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the list of obstacles present in the world.
     *
     * @return List of obstacles.
     */
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    /**
     * Returns the visibility range for robots in the world.
     *
     * @return Visibility range in units.
     */
    public int getVisibilityRange() {
        return visibilityRange;
    }

    /**
     * Returns the maximum shield strength allowed for robots.
     *
     * @return Maximum shield strength.
     */
    public int getMaxShieldStrength() {
        return maxShieldStrength;
    }


    public void addObstacle(Pit Pit) {
        obstacles.add(Pit);
    }

    public void addObstacle(Lake Lake) {
        obstacles.add(Lake);
    }

    public void addObstacle(Mountain mountain) {
        obstacles.add(mountain);
    }

    public void addObstacle(Mine mine) {
        obstacles.add(mine);
    }

    /**
     * Returns the reload time for robot weapons.
     *
     * @return Reload time in ticks.
     */
    public int getReloadTime() {
        return reloadTime;
    }

    /**
     * Returns the maximum number of shots a robot can fire before reloading.
     *
     * @return Maximum shots.
     */
    public int getMaxShots() {
        return maxShots;
    }

    /**A
     * Retrieves a robot at the specified position.
     *
     * @param pos The position to check.
     * @return The robot at the position, or null if none exists.
     */
    public Robot getRobotAtPosition(Position pos) {
        for (Robot r : robots) {
            if (r.getPosition().equals(pos)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Calculates the next position in the given direction from the current position.
     *
     * @param pos The starting position.
     * @param dir The direction to move.
     * @return The next position in the specified direction.
     */
    private Position getNextPosition(Position pos, Direction dir) {
        int x = pos.getX();
        int y = pos.getY();
        switch (dir) {
            case NORTH: return new Position(x, y + 1);
            case EAST: return new Position(x + 1, y);
            case SOUTH: return new Position(x, y - 1);
            case WEST: return new Position(x - 1, y);
            default: throw new IllegalArgumentException("Invalid direction");
        }
    }

    /**
     * Finds the closest obstacle or robot in the specified direction within the max distance.
     *
     * @param start The starting position.
     * @param dir The direction to look.
     * @param maxDistance The maximum distance to scan.
     * @param lookingRobot The robot performing the look, to avoid self-detection.
     * @return An ObjectInDirection with type and distance, or null if none found.
     */
    public ObjectInDirection findClosestObjectInDirection(Position start, Direction dir, int maxDistance, Robot lookingRobot) {
        Position current = start;
        for (int i = 1; i <= maxDistance; i++) {
            current = getNextPosition(current, dir);
            if (!isPositionValid(current)) {
                return null; // Reached edge
            }
            if (isPositionBlocked(current)) {
                return new ObjectInDirection("OBSTACLE", i);
            }
            Robot robotAtPos = getRobotAtPosition(current);
            if (robotAtPos != null && !robotAtPos.equals(lookingRobot)) {
                return new ObjectInDirection("ROBOT", i);
            }
        }
        return null;
    }

    public String displayJson(){
        JsonObject worldDetails = new JsonObject();
        worldDetails.addProperty("world size", "%d x %d".formatted(getHeight(), getWidth()));
        JsonArray obstacleList = new JsonArray();

        for (Obstacle obs:getObstacles()){
            JsonObject obstacleObject = new JsonObject();
            obstacleObject.addProperty("Obstacle type", obs.getType());
            obstacleObject.addProperty("BottomLeft-X", obs.getBottomLeftX());
            obstacleObject.addProperty("BottomLeft-Y", obs.getBottomLeftY());
            obstacleList.add(obstacleObject);
        }

        worldDetails.add("Obstacles", obstacleList);

        return worldDetails.toString();
    }

}
