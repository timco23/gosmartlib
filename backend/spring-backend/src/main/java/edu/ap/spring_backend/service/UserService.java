package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.ClassDTO;
import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.dto.UserResponse;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.jpa.Role;

import java.util.List;

@Service
public class UserService {
        private final UserRepository userRepository;
        private final ItemRepository itemRepository;
        private final SchoolRepository schoolRepository;

        public UserService(UserRepository userRepository, ItemRepository itemRepository, SchoolRepository schoolRepository) {
                this.userRepository = userRepository;
                this.itemRepository = itemRepository;
                this.schoolRepository = schoolRepository;
        }

        public UserResponse getOwnProfile(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return buildUserResponse(user);
        }

        public UserResponse getProfile(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return buildUserResponse(user);
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

        private ClassDTO mapToClassDTO(SchoolClass clazz) {
                return new ClassDTO(clazz.getClassName(), clazz.getSchool().getId());
        }

        public List<UserResponse> getUsersBySchool(Long schoolId) {
                return userRepository.findBySchoolId(schoolId).stream()
                                .map(this::buildUserResponse)
                                .toList();
        }

        public List<UserResponse> getUsersByCurrentSchool(Integer userId) {
                User requester = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                if (requester.getSchool() == null) {
                        return List.of();
                }
                return getUsersBySchool(requester.getSchool().getId());
        }

        public UserResponse addToItemsBorrowed(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                user.setItemsBorrowed(user.getItemsBorrowed() + 1);
                userRepository.save(user);
                return buildUserResponse(user);
        }

        public UserResponse removeFromItemsBorrowed(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                user.setItemsBorrowed(Math.max(0, user.getItemsBorrowed() - 1));
                userRepository.save(user);
                return buildUserResponse(user);
        }

        public UserResponse updateUserRole(Integer requesterUserId, String targetUserEmail, String newRoleStr) {
                User requester = userRepository.findById(requesterUserId)
                                .orElseThrow(() -> new RuntimeException("Requester not found"));
                Role requesterRole = requester.getRole();
                if (requesterRole != Role.BIBLIOTHEEKBEHEERDER && requesterRole != Role.ADMIN) {
                        throw new SecurityException("Insufficient permissions");
                }
                Role newRole;
                try {
                        newRole = Role.valueOf(newRoleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid role: " + newRoleStr);
                }
                User target = userRepository.findByEmail(targetUserEmail)
                                .orElseThrow(EntityNotFoundException::new);

                if (requester.getId().equals(target.getId())) {
                        throw new SecurityException("Cannot change your own role");
                }

                if (requesterRole == Role.BIBLIOTHEEKBEHEERDER) {
                        if (target.getRole() == Role.ADMIN) {
                                throw new SecurityException("Bibliotheekbeheerder cannot change the role of an admin");
                        }
                        if (newRole == Role.ADMIN) {
                                throw new SecurityException("Bibliotheekbeheerder cannot assign ADMIN role");
                        }
                        Long requesterSchoolId = requester.getSchool() != null ? requester.getSchool().getId() : null;
                        Long targetSchoolId = target.getSchool() != null ? target.getSchool().getId() : null;
                        if (requesterSchoolId == null || !requesterSchoolId.equals(targetSchoolId)) {
                                throw new SecurityException("Can only update users from the same school");
                        }
                }

                target.setRole(newRole);
                userRepository.save(target);
                return buildUserResponse(target);
        }

        public UserResponse changeUserSchool(String email, Long schoolId) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(EntityNotFoundException::new);
                School school = schoolRepository.findById(schoolId)
                                .orElseThrow(EntityNotFoundException::new);
                user.setSchool(school);
                userRepository.save(user);
                return buildUserResponse(user);
        }

        public UserResponse buildUserResponse(User user) {
                return new UserResponse(
                                user.getId(),
                                user.getName(),
                                user.getFirstName(),
                                user.getEmail(),
                                user.getRole().name(),
                                user.getStudentNumber(),
                                user.getClasses().stream().map(this::mapToClassDTO).toList(),
                                user.getSchool() != null ? user.getSchool().getName() : null,
                                user.getSchool() != null ? user.getSchool().getId() : null,
                                user.getFavoriteBooks().stream().map(this::mapToItemDTO).toArray(ItemDTO[]::new),
                                user.getItemsBorrowed());
        }
}
