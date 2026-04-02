package edu.ap.spring_backend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public record LendingRequestDTO(
        Integer borrowerId,
        String copyBarcode,
        Long schoolId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate lendingDate,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate returnDate
) { }
