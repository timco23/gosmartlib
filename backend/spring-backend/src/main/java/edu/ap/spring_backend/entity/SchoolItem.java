package edu.ap.spring_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name="School_Item")
public class SchoolItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private int schoolId;
    private String tags;
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId", unique = true)
    private Item item;
    private boolean featured;

    public SchoolItem() {}

    public SchoolItem(int schoolId, String tags, String description, boolean featured, Item item) {
        this.schoolId = schoolId;
        this.tags = tags;
        this.description = description;
        this.featured = featured;
        this.item = item;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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
