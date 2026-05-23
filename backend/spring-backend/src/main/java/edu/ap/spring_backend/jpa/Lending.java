package edu.ap.spring_backend.jpa;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Lending {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDate lendingDate;

    @Column
    private LocalDate returnDate;
    
    @Column()
    private boolean returned;
    
    @Column()
    private LocalDate dateReturned;
    
    @Column()
    private boolean extended;

    @Column(nullable = false)
    private int extendCount = 0;

    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = true)
    private User borrower;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private SchoolItemCopy book;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "snap_class_name", length = 50)
    private String snapClassName;

    @Column(name = "snap_campus_id")
    private Long snapCampusId;

    @Column(name = "snap_campus_name", length = 100)
    private String snapCampusName;

    @Column(name = "snap_school_year", length = 9, nullable = false)
    private String snapSchoolYear;

    @Column(name = "snap_reading_level", length = 20)
    private String snapReadingLevel;

    @Column(name = "snap_book_title", length = 255)
    private String snapBookTitle;

    @Column(name = "snap_book_genre", length = 50)
    private String snapBookGenre;

    @Column(name = "snap_book_isbn", length = 20)
    private String snapBookIsbn;

    public boolean isReturned() { return Boolean.TRUE.equals(returned); }
    public boolean isExtended() { return Boolean.TRUE.equals(extended); }

    public Lending() {
    }

    public Lending(User borrower, SchoolItemCopy book, LocalDate lendingDate, LocalDate returnDate, Boolean returned, LocalDate dateReturned) {
        this.borrower = borrower;
        this.book = book;
        this.lendingDate = lendingDate;
        this.returnDate = returnDate;
        this.returned = returned;
        this.dateReturned = dateReturned;
    }
}
