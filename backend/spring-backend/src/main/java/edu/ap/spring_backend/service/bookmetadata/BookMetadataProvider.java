package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;

import java.util.Optional;

public interface BookMetadataProvider {
    String getName();
    boolean isEnabled();
    Optional<BookMetadata> fetchByIsbn(String isbn);
}
