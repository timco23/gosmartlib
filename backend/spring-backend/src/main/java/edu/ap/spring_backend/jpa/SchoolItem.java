package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="school_item")
public class SchoolItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;
    private String tags;
    @Column(name = "book_description", columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ItemId", unique = true)
    private Item item;
    private boolean featured;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    //each tag is a separate row in the join table "school_item_id"
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "school_item_tag",
            joinColumns = @JoinColumn(name = "school_item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public SchoolItem() {}

    public SchoolItem(School school, String tags, String description, boolean featured, Item item, Campus campus) {
        this.school = school;
        this.tags = tags;
        this.description = description;
        this.featured = featured;
        this.item = item;
        this.campus = campus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSchoolId() { return schoolId; }
    public void setSchoolId(int schoolId) { this.schoolId = schoolId; }

    public School getSchool() {
        return school;
    }

    public void setSchoolId(School school) {
        this.school = school;
    }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() {
        return tags;
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

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }
}
