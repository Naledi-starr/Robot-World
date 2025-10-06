// WorldSetup.java
package za.co.wethinkcode.server.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "world_properties")
public class WorldProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Optional primary key

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    private SavedWorld world;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obstacle_id", nullable = false)
    private ObstacleType obstacleType;

    @Column(name = "x_cord", nullable = false)
    private int x;

    @Column(name = "y_cord", nullable = false)
    private int y;

    @Column(name = "obstacle_width", nullable = false)
    private int width;

    @Column(name = "obstacle_height", nullable = false)
    private int height;

    public WorldProperty() {}

    public WorldProperty(ObstacleType obstacleType, int xCord, int yCord) {
        this.obstacleType = obstacleType;
        this.x = xCord;
        this.y = yCord;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SavedWorld getWorld() {
        return world;
    }

    public void setWorld(SavedWorld world) {
        this.world = world;
    }

    public ObstacleType getObstacleType() {
        return obstacleType;
    }

    public void setObstacleType(ObstacleType obstacleType) {
        this.obstacleType = obstacleType;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getObstacleName() {
        ObstacleType obstacleType = getObstacleType();
        return  obstacleType.getName();
    }

    public int getObstacleId() {
        if (obstacleType == null || obstacleType.getId() == null) {
            throw new RuntimeException("world has no obstacles");
        }
        return obstacleType.getId();
    }
}
