package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.dto.UserResponse;

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
                                item.getGenres() != null ? item.getGenres().stream().map(g -> (long) g.getId()).toList()
                                                : List.of(),
                                item.getSubgenres() != null
                                                ? item.getSubgenres().stream().map(s -> (long) s.getId()).toList()
                                                : List.of(),
                                item.getReadingLevel() != null ? (long) item.getReadingLevel().getId() : null,
                                item.getDidacticCategories() != null
                                                ? item.getDidacticCategories().stream().map(d -> (long) d.getId())
                                                                .toList()
                                                : List.of(),
                                null, // schoolId (of juiste waarde)
                                false, // featured (of uit copy halen)
                                item.isDidactic(),
                                null // campusId
                );
        }

        public List<UserResponse> getUsersBySchool(Long schoolId) {
                return userRepository.findBySchoolId(schoolId).stream()
                                .map(user -> new UserResponse(
                                                user.getId(),
                                                user.getName(),
                                                user.getFirstName(),
                                                user.getEmail(),
                                                user.getRole().name(),
                                                user.getStudentNumber(),
                                                user.getClassName(),
                                                user.getSchool() != null ? user.getSchool().getName() : null,
                                                user.getFavoriteBooks().toArray(new Item[0]),
                                                user.getItemsBorrowed()))
                                .toList();
        }

        public UserResponse addToItemsBorrowed(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                user.setItemsBorrowed(user.getItemsBorrowed() + 1);
                userRepository.save(user);
                return new UserResponse(
                                user.getId(),
                                user.getName(),
                                user.getFirstName(),
                                user.getEmail(),
                                user.getRole().name(),
                                user.getStudentNumber(),
                                user.getClassName(),
                                user.getSchool() != null ? user.getSchool().getName() : null,
                                user.getFavoriteBooks().toArray(new Item[0]),
                                user.getItemsBorrowed());
        }

        public UserResponse removeFromItemsBorrowed(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                user.setItemsBorrowed(Math.max(0, user.getItemsBorrowed() - 1));
                userRepository.save(user);
                return new UserResponse(
                                user.getId(),
                                user.getName(),
                                user.getFirstName(),
                                user.getEmail(),
                                user.getRole().name(),
                                user.getStudentNumber(),
                                user.getClassName(),
                                user.getSchool() != null ? user.getSchool().getName() : null,
                                user.getFavoriteBooks().toArray(new Item[0]),
                                user.getItemsBorrowed());
        }
}
