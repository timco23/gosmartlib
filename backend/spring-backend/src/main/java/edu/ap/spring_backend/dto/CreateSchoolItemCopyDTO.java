package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.ItemCondition;

public record CreateSchoolItemCopyDTO(
        @JsonProperty("itemId")
        Long itemId,

        @JsonProperty("condition")
        ItemCondition condition,

        @JsonProperty("copy_status")
        CopyStatus copy_status,

        @JsonProperty("featured")
        boolean featured,

        @JsonProperty("schoolId")
        Long schoolId,

        @JsonProperty("campusId")
        Long campusId
) { }