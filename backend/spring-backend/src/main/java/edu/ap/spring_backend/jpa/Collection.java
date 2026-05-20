package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "collection")
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Boolean featured;
    private Boolean globallyFeatured;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "collection_item",  joinColumns = @JoinColumn(name = "collection_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.EAGER )
    @JoinColumn(name="school_id")
    private School school;

    public Collection() {}

    public Collection(String title, String description, School school, List<Item> items, Boolean featured, Boolean globallyFeatured) {
        this.title = title;
        this.description = description;
        this.school = school;
        this.items = items;
        this.featured = featured;
        this.globallyFeatured = globallyFeatured;
    }
}
