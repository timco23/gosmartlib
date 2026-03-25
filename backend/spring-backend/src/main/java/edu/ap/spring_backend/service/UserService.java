package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.TagDTO;
import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.User;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public UserService(UserRepository userRepository, ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public ItemDTO[] getFavoriteBooks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFavoriteBooks().stream().map(item -> mapToItemDTO(item)).toArray(ItemDTO[]::new);
    }

    public void addFavoriteBook(String email, Long itemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        user.getFavoriteBooks().add(item);
        userRepository.save(user);
    }

    public void deleteFavoriteBook(String email, Long itemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        user.getFavoriteBooks().remove(item);
        userRepository.save(user);
    }

    private ItemDTO mapToItemDTO(Item item) {
        List<TagDTO> tags = item.getTags().stream()
                .map(tag -> new TagDTO((long) tag.getId(), tag.getName()))
                .toList();
        return new ItemDTO(
                item.getId(),
                item.getTitle(),
                item.getPublisher(),
                item.getAuthor(),
                item.getIsbn(),
                item.getReleaseYear(),
                item.getBookLanguage(),
                item.getPageCount(),
                item.getCover(),
                item.getReviewScore(),
                item.getReviewCount(),
                item.getDescription(),
                tags,
                item.getGenres() != null ? item.getGenres().stream().map(g -> (long) g.getId()).toList() : List.of(),
                item.getSubgenres() != null ? item.getSubgenres().stream().map(s -> (long) s.getId()).toList()
                        : List.of(),
                item.getReadingLevel() != null ? (long) item.getReadingLevel().getId() : null,
                item.getDidacticCategories() != null
                        ? item.getDidacticCategories().stream().map(d -> (long) d.getId()).toList()
                        : List.of(),
                null, // schoolId (of juiste waarde)
                false, // featured (of uit copy halen)
                item.isDidactic(),
                null // campusId
        );
    }
}
