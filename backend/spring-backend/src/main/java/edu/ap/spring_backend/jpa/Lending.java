package edu.ap.spring_backend.jpa;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
    @GeneratedValue
    private Long id;
    
    @Column()
    private LocalDate lendingDate;
    
    @Column()
    private LocalDate returnDate;
    
    @Column()
    private Boolean returned;
    
    @Column()
    private LocalDate dateReturned;
    
    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;
    
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private SchoolItemCopy book;
    
    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
    
    public Lending() {
    }
    
    public Lending(User borrower, SchoolItemCopy book, LocalDate lendingDate, LocalDate returnDate, Boolean returned,
        LocalDate dateReturned) {
            this.borrower = borrower;
            this.book = book;
        this.lendingDate = lendingDate;
        this.returnDate = returnDate;
        this.returned = returned;
        this.dateReturned = dateReturned;
    }
}
