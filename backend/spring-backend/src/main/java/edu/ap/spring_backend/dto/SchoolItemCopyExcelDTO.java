package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SchoolItemCopyExcelDTO(
        @JsonProperty("rows")
        SchoolItemCopyExcelRowDTO[] rows
) {
}
