package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String publisher;
    private String author;
    private String isbn;
    private int releaseYear;
    private String bookLanguage;
    private int pageCount;
    private String cover;
    private double reviewScore;
    private int reviewCount;
    @Column(name = "book_description", columnDefinition = "TEXT")
    private String description;

    //each tag is a separate row in the join table "item_id"
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "item_tag",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<SchoolItemCopy> copies;


    public Item() {}

    public Item(String description, int reviewCount, double reviewScore, String cover, int pageCount, String bookLanguage, int releaseYear, String isbn, String author, String publisher, String title, Set<Tag> tags) {
        this.description = description;
        this.reviewCount = reviewCount;
        this.reviewScore = reviewScore;
        this.cover = cover;
        this.pageCount = pageCount;
        this.bookLanguage = bookLanguage;
        this.releaseYear = releaseYear;
        this.isbn = isbn;
        this.author = author;
        this.publisher = publisher;
        this.title = title;
        this.tags = tags;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getBookLanguage() {
        return bookLanguage;
    }

    public void setBookLanguage(String bookLanguage) {
        this.bookLanguage = bookLanguage;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public double getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(double reviewScore) {
        this.reviewScore = reviewScore;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
