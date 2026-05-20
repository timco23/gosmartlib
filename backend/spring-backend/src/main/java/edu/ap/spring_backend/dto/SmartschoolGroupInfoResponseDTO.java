package edu.ap.spring_backend.dto;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SmartschoolGroupInfoResponseDTO(
        @JsonProperty("groups")
        SmartschoolGroupDTO[] groups,
        @JsonProperty("parentGroups")
        SmartschoolGroupDTO[] parentGroups
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmartschoolGroupInfoResponseDTO(var groups, var parentGroups))) return false;
        return Arrays.equals(groups(), groups)
                && Arrays.equals(parentGroups(), parentGroups);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(groups);
        result = 31 * result + Arrays.hashCode(parentGroups);
        return result;
    }

    @Override
    public String toString() {
        return "SmartschoolGroupInfoResponseDTO[groups=" + Arrays.toString(groups)
                + ", parentGroups=" + Arrays.toString(parentGroups) + "]";
    }
}
