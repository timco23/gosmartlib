package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SchoolItemCopyDTO(
        @JsonProperty("id")
        int id,

        @JsonProperty("schoolItemId")
        int schoolItemId,

        @JsonProperty("barcode")
        String barcode,

        @JsonProperty("condition")
        String condition
) { }