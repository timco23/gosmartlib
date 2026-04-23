package edu.ap.spring_backend.dto;

import java.util.List;

public record ItemEnrichmentEvent(List<TargetItem> items) {
    public record TargetItem(long itemId, String isbn) {}
}
