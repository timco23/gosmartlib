package edu.ap.spring_backend.dto;

import edu.ap.spring_backend.jpa.Genre;
import edu.ap.spring_backend.jpa.Subgenre;

public record SubgenreDTO(Long id, String name, Long genreId) {
    public static SubgenreDTO from(Subgenre subgenre) {
        if (subgenre == null) return null;
        Long gid = subgenre.getGenre() != null ? subgenre.getGenre().getId() : null;
        return new SubgenreDTO(subgenre.getId(), subgenre.getName(), gid);
    }

    public Subgenre toEntity() {
        Subgenre s = new Subgenre();
        s.setId(id);
        s.setName(name);
        if (genreId != null) {
            Genre g = new Genre();
            g.setId(genreId);
            s.setGenre(g);
        }
        return s;
    }
}