package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name="school_item")
public class SchoolItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;
    @Column(name = "book_description", columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ItemId", unique = true)
    private Item item;
    private boolean featured;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;
    

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "school_item_tag", joinColumns = @JoinColumn(name = "school_item_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "school_item_genre", joinColumns = @JoinColumn(name = "school_item_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "school_item_subgenre", joinColumns = @JoinColumn(name = "school_item_id"), inverseJoinColumns = @JoinColumn(name = "subgenre_id"))
    private Set<Subgenre> subgenres = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reading_level_id")
    private ReadingLevel readingLevel;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "school_item_didactic_category", joinColumns = @JoinColumn(name = "school_item_id"), inverseJoinColumns = @JoinColumn(name = "didactic_category_id"))
    private Set<DidacticCategory> didacticCategories = new HashSet<>();

    public SchoolItem() {
    }

    public SchoolItem(School school, Set<Tag> tags, Set<Genre> genres, Set<Subgenre> subgenres,
            ReadingLevel readingLevel, Set<DidacticCategory> didacticCategories,
            String description, boolean featured, Item item, Campus campus) {
        this.school = school;
        this.tags = tags;
        this.genres = genres;
        this.subgenres = subgenres;
        this.readingLevel = readingLevel;
        this.didacticCategories = didacticCategories;
        this.description = description;
        this.featured = featured;
        this.item = item;
        this.campus = campus;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Set<Subgenre> getSubgenres() {
        return subgenres;
    }

    public void setSubgenres(Set<Subgenre> subgenres) {
        this.subgenres = subgenres;
    }

    public ReadingLevel getReadingLevel() {
        return readingLevel;
    }

    public void setReadingLevel(ReadingLevel readingLevel) {
        this.readingLevel = readingLevel;
    }

    public Set<DidacticCategory> getDidacticCategories() {
        return didacticCategories;
    }

    public void setDidacticCategories(Set<DidacticCategory> didacticCategories) {
        this.didacticCategories = didacticCategories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}