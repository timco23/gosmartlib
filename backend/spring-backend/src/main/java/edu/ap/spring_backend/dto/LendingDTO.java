package edu.ap.spring_backend.dto;

public record LendingDTO(
    Long id,
    String borrowerName,
    String bookTitle,
    String bookAuthor,
    String schoolName,
    String lendingDate,
    String returnDate,
    String dateReturned,
    Boolean returned
) { }