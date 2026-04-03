package edu.ap.spring_backend.dto;

public record LendingDTO(
    Long id,
    String borrowerName,
    Long borrowerId,
    String bookTitle,
    String bookAuthor,
    Long schoolId,
    String lendingDate,
    String returnDate,
    String dateReturned,
    Boolean returned,
    String copyBarcode,
    Boolean extended
) { }