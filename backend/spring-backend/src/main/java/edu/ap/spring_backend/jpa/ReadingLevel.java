package edu.ap.spring_backend.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ReadingLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(length = 1000, nullable = false)
    private String description;

    // constructors
    public ReadingLevel() {
    }

    public ReadingLevel(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
