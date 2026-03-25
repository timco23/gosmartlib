package edu.ap.spring_backend.dto;

import java.util.List;

public record GenreWithSubgenresDTO(Long id, String name, List<SubgenreDTO> subgenres) {}