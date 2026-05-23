package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class BulkImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false)
    private LocalDateTime importedAt;

    @Column(nullable = false)
    private int importedBooks;

    @Column(nullable = false)
    private int importedCopies;

    @ElementCollection
    @CollectionTable(name = "bulk_import_history_copy_ids", joinColumns = @JoinColumn(name = "history_id"))
    @Column(name = "copy_id")
    private List<Integer> copyIds;

    public BulkImportHistory() {}

    public BulkImportHistory(School school, LocalDateTime importedAt, int importedBooks, int importedCopies, List<Integer> copyIds) {
        this.school = school;
        this.importedAt = importedAt;
        this.importedBooks = importedBooks;
        this.importedCopies = importedCopies;
        this.copyIds = copyIds;
    }
}
