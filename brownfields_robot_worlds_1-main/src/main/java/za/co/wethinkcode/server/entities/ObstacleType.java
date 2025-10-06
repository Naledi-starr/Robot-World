// ObstacleType.java
package za.co.wethinkcode.server.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "obstacles")
public class ObstacleType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "obstacle_name", nullable = false, unique = true)
    private String name;

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
