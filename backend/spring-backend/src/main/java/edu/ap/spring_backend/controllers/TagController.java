package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.jpa.Tag;
import edu.ap.spring_backend.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<Tag> getAll() {
        return tagService.getAll();
    }

    @PostMapping
    public Tag create(@RequestBody TagDTO tag) {
        return tagService.create(tag);
    }

    @PutMapping("/{id}")
    public Tag update(@PathVariable Long id, @RequestBody TagDTO tag) {
        return tagService.update(id, tag);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }
}
