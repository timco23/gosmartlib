package edu.ap.spring_backend.dto;

import java.util.List;

public record GenreWithSubgenresDTO(int id, String name, List<SubgenreDTO> subgenres) {}