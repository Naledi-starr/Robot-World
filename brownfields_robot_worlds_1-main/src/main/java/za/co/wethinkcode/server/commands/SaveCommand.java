package za.co.wethinkcode.server.commands;

import org.hibernate.Session;
import org.hibernate.Transaction;
import za.co.wethinkcode.server.entities.*;
import za.co.wethinkcode.server.utils.HibernateUtil;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldDumper;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.sql.*;
import java.util.List;
import java.util.Scanner;
import java.time.*;

public class SaveCommand implements Command {

    private final World world;
    private final WorldDumper worldDumper;


    public SaveCommand(World world) {
        this.world = world;
        this.worldDumper = new WorldDumper(world);
    }

    @Override
    public String execute() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Name the world to save/Hit enter to save by default name: ");
        String worldName = scanner.nextLine().toLowerCase().trim();

        int worldSize = Math.max(world.getWidth(), world.getHeight());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Default world name if empty
            if (worldName.isEmpty()) {
                worldName = generateNextDefaultName(session);
            }

            // Delete existing world with same name
            SavedWorld existing = session.createQuery(
                            "FROM SavedWorld WHERE worldName = :name", SavedWorld.class)
                    .setParameter("name", worldName)
                    .uniqueResult();
            if (existing != null) {
                session.remove(existing);
                session.flush();
            }

            // if 'worldName' >= 2 replace whitespace with '_'
            if (worldName.length() >= 2) {
                worldName = worldName.replaceAll("\\s+", "_");
            }

            // Create new SavedWorld
            SavedWorld savedWorld = new SavedWorld();
            savedWorld.setWorldName(worldName);
            savedWorld.setWorldSize(worldSize);
            savedWorld.setCreatedAt(LocalDateTime.now());

            // Attach obstacles
            for (Obstacle obstacle : world.getObstacles()) {
                ObstacleType obstacleType = session.createQuery(
                                "FROM ObstacleType WHERE name = :name", ObstacleType.class)
                        .setParameter("name", obstacle.getType().toLowerCase())
                        .uniqueResult();

                if (obstacleType == null) {
                    throw new RuntimeException("Unknown obstacle: " + obstacle.getType());
                }

                WorldProperty ws = new WorldProperty();
                ws.setWorld(savedWorld);
                ws.setObstacleType(obstacleType);
                ws.setX(obstacle.getX());
                ws.setY(obstacle.getY());
                ws.setWidth(obstacle.getWidth());
                ws.setHeight(obstacle.getHeight());

                savedWorld.addObstacle(ws);
            }

            session.persist(savedWorld);
            tx.commit();

            return "✅ World \"" + worldName + "\" saved successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Failed to save world: " + e.getMessage();
        }
    }

    private String generateNextDefaultName(Session session) {
        List<String> names = session.createQuery(
                "SELECT w.worldName FROM SavedWorld w", String.class).list();

        int nextNumber = 1;
        for (String name : names) {
            if (name != null && name.toLowerCase().startsWith("world")) {
                try {
                    int num = Integer.parseInt(name.substring(5).trim());
                    if (num >= nextNumber) {
                        nextNumber = num + 1;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "world" + nextNumber;
    }


    // Will let the user decide what to do when name selected already exists and returns the newly/oldly selected name.
    private String promptWorldName(Connection conn, String worldName) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        while (worldNameExists(conn, worldName)) {
            System.out.println("World already exists, would you like to overwrite ( Yes/No ):");
            String response = scanner.nextLine();
            if (response.equalsIgnoreCase("yes")){
                return worldName;
            }
            System.out.println("Name the world to save/Hit enter to save by default name: ");
            String newName = scanner.nextLine();
            if (newName.equals(worldName)){
                promptWorldName(conn, newName);
            }
            if (worldName == null || worldName.isEmpty()){
                worldName = generateNextDefaultName(conn);
            }
        }
        return worldName;
    }

    public int getObstacleId(Connection conn, String obstacleName) throws SQLException {
        String sql = "SELECT id FROM obstacles WHERE obstacle_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, obstacleName.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Obstacle type not found: " + obstacleName);
            }
        }
    }

    public String generateNextDefaultName(Connection conn) throws SQLException {
        String sql = "SELECT world_name FROM saved_worlds";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int nextNumber = 1;

            // Loop through all existing world names
            while (rs.next()) {
                String name = rs.getString("world_name");  // ✅ Correct column name

                // Check if the name matches "world1", "world2", etc.
                if (name != null && name.toLowerCase().startsWith("world")) {
                    try {
                        // Extract the number part
                        String numPart = name.substring(5);  // Remove "world"
                        int num = Integer.parseInt(numPart.trim());

                        // If this number is >= nextNumber, increment
                        if (num >= nextNumber) {
                            nextNumber = num + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore names like "world" (no number) or "worldabc"
                        continue;
                    }
                }
            }

            return "world" + nextNumber;
        }
    }

    public boolean worldNameExists(Connection conn, String worldName) throws SQLException{
        String sql = "SELECT world_name FROM saved_worlds";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                String name = rs.getString("world_name");
                if (name.equals(worldName)){
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String display() {
        return "Saves current world state into database.";
    }
}
