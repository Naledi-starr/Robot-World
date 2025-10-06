// WorldConfig.java
package za.co.wethinkcode.server.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saved_worlds")
public class SavedWorld {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "world_name", nullable = false, unique = true)
    private String worldName;

    @Column(name = "world_size", nullable = false)
    private Integer worldSize;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorldProperty> obstacles = new ArrayList<>();

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Integer getWorldSize() {
        return worldSize;
    }

    public void setWorldSize(Integer worldSize) {
        this.worldSize = worldSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<WorldProperty> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<WorldProperty> obstacles) {
        this.obstacles = obstacles;
    }

    public void addObstacle(WorldProperty obstacle) {
        obstacles.add(obstacle);
        obstacle.setWorld(this);
    }

    public void removeObstacle(WorldProperty obstacle) {
        obstacles.remove(obstacle);
        obstacle.setWorld(null);
    }
}
