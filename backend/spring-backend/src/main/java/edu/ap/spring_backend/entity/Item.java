package edu.ap.spring_backend.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Item")
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
    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER)
    private List<SchoolItem> schoolItems;

    public Item() {}

    public Item(String title, String publisher, String author, String isbn, int releaseYear, String bookLanguage, int pageCount, String cover, double reviewScore, int reviewCount) {
        this.title = title;
        this.publisher = publisher;
        this.author = author;
        this.isbn = isbn;
        this.releaseYear = releaseYear;
        this.bookLanguage = bookLanguage;
        this.pageCount = pageCount;
        this.cover = cover;
        this.reviewScore = reviewScore;
        this.reviewCount = reviewCount;
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
}
