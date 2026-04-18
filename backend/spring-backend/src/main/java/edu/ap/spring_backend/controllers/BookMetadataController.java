package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.service.bookmetadata.BookMetadataService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/books/metadata")
public class BookMetadataController {

    private final BookMetadataService bookMetadataService;

    public BookMetadataController(BookMetadataService bookMetadataService) {
        this.bookMetadataService = bookMetadataService;
    }

    // Used by the frontend add-book form to autofill fields from an ISBN.
    @GetMapping
    public ResponseEntity<BookMetadata> getByIsbn(
            @RequestParam @Pattern(regexp = "\\d{13}") String isbn) {
        return bookMetadataService.fetchByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
