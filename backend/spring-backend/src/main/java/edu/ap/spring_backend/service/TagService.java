package edu.ap.spring_backend.service;

import edu.ap.spring_backend.DAO.TagRepository;
import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.jpa.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public Tag create(TagDTO newTag) {
        if (tagRepository.existsByNameIgnoreCase(newTag.name())) {
            throw new IllegalArgumentException("Tag bestaat reeds");
        }
        Tag tag = new Tag();
        tag.setName(newTag.name());
        return tagRepository.save(tag);
    }

    public Tag update(Long id, TagDTO newTag) {
        Tag existing = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag niet gevonden"));

        if (existing.isDefault()) {
            throw new IllegalArgumentException("Standaard tags kunnen niet gewijzigd worden");
        }
        if (tagRepository.existsByNameIgnoreCaseAndIdNot(newTag.name(), id)) {
            throw new IllegalArgumentException("Tag bestaat reeds");
        }
        existing.setName(newTag.name());
        return tagRepository.save(existing);
    }

    public void delete(Long id) {
        Tag existing = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag niet gevonden"));

        if (existing.isDefault()) {
            throw new IllegalArgumentException("Standaard tags kunnen niet verwijderd worden");
        }
        tagRepository.delete(existing);
    }
}
