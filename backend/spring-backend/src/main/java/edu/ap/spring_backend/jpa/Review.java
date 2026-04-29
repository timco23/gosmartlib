package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

/**
 * JPA entity that represents a review on an item.
 */
@Entity
@Getter
@Setter
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = true, length = 800)
    private String description;

    @Column(nullable = false)
    private int stars;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isAnonymous; 

    // If createdAt is still null then sets createdAt to LocalDate.now()
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDate.now();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;


    @Column(nullable = false)
    private int reportCount = 0;

    public Review() {}

    /**
     * Creates a review and sets the creation date to today.
     */
    public Review(String description, int stars, Item item, User author, boolean isAnonymous) {
        this.description = description;
        this.stars = stars;
        this.item = item;
        this.author = author;
        this.isAnonymous = isAnonymous;
        this.createdAt = LocalDate.now();
    }
}