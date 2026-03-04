package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Entity class representing a campus in the system.
 * Contains campus credentials.
 */
@Entity
@Table(name = "campus")
public class Campus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonBackReference
    private School school;
    
    public Campus() {}
    
    public Campus(String name, School school) {
        this.name = name;
        this.school = school;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public School getSchool() { return school; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSchool(School school) { this.school = school; }
}