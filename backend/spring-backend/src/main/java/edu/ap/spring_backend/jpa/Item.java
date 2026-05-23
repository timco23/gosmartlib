package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String publisher;

    @NotBlank
    @Size(max = 255)
    private String author;

    @Size(max = 20)
    private String isbn;

    @Min(0)
    private int releaseYear;

    @NotBlank
    private String bookLanguage;

    @Min(0)
    private int pageCount;

    @Size(max = 255)
    private String cover;

    @Min(0)
    @Max(10)
    private double reviewScore;

    @Min(0)
    private int reviewCount;
    @Column(name = "book_description", columnDefinition = "TEXT")
    private String description;

    private boolean updating;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "item_tag", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<SchoolItemCopy> copies;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "item_genre", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "item_subgenre", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "subgenre_id"))
    private Set<Subgenre> subgenres = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reading_level_id")
    private ReadingLevel readingLevel;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "item_didactic_category", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "didactic_category_id"))
    private Set<DidacticCategory> didacticCategories = new HashSet<>();

    @Column(name = "is_didactic", nullable = false)
    private boolean didactic;

    public Item() {
    }

    public Item(String description, int reviewCount, double reviewScore, String cover, int pageCount,
            String bookLanguage, int releaseYear, String isbn, String author, String publisher, String title,
            Set<Tag> tags) {
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
}
