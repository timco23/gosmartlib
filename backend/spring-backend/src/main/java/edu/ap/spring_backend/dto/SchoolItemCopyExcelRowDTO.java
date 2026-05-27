package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SchoolItemCopyExcelRowDTO(
        @JsonProperty("itemName")
        String itemName,
        @JsonProperty("copyId")
        String copyId
) {
}
