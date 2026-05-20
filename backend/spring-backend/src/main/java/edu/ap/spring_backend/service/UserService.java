package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.UserResponse;
import org.springframework.stereotype.Service;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.mappers.ItemMapper;
import edu.ap.spring_backend.mappers.UserMapper;
import edu.ap.spring_backend.jpa.Role;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public UserService(UserRepository userRepository, ItemRepository itemRepository,
            SchoolRepository schoolRepository, UserMapper userMapper, ItemMapper itemMapper) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
    }

    private User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);
    }

    public UserResponse getOwnProfile(Integer userId) {
        return userMapper.toUserResponse(findUserById(userId));
    }

    public UserResponse getProfile(String email) {
        return userMapper.toUserResponse(findUserByEmail(email));
    }

    public ItemDTO[] getFavoriteBooks(String email) {
        return findUserByEmail(email).getFavoriteBooks().stream()
                .map(itemMapper::toItemDTO)
                .toArray(ItemDTO[]::new);
    }

    public void addFavoriteBook(String email, Long itemId) {
        User user = findUserByEmail(email);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        user.getFavoriteBooks().add(item);
        userRepository.save(user);
    }

    public void deleteFavoriteBook(String email, Long itemId) {
        User user = findUserByEmail(email);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        user.getFavoriteBooks().remove(item);
        userRepository.save(user);
    }

    public ItemDTO[] getWishlistedBooks(String email){
        return findUserByEmail(email).getWishlistedBooks().stream()
                .map(itemMapper::toItemDTO)
                .toArray(ItemDTO[]::new);
    }

    public void addWishlistedBook(String email, Long itemId) {
        User user = findUserByEmail(email);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        user.getWishlistedBooks().add(item);
        userRepository.save(user);
    }

    public void deleteWishlistedBook(String email, Long itemId) {
        User user = findUserByEmail(email);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        user.getWishlistedBooks().remove(item);
        userRepository.save(user);
    }

    public List<UserResponse> getUsersBySchool(Long schoolId) {
        return userRepository.findBySchoolId(schoolId).stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public List<UserResponse> getUsersByCurrentSchool(Integer userId) {
        User requester = findUserById(userId);
        if (requester.getSchool() == null)
            return List.of();
        return getUsersBySchool(requester.getSchool().getId());
    }

    public boolean userHasSchool(String email) {
        User user = findUserByEmail(email);
        return user.getSchool() != null;
    }

    public UserResponse addToItemsBorrowed(Integer userId) {
        User user = findUserById(userId);
        user.setItemsBorrowed(user.getItemsBorrowed() + 1);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public UserResponse removeFromItemsBorrowed(Integer userId) {
        User user = findUserById(userId);
        user.setItemsBorrowed(Math.max(0, user.getItemsBorrowed() - 1));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUserRole(Integer requesterUserId, String targetUserEmail, String newRoleStr) {
        User requester = findUserById(requesterUserId);
        Role requesterRole = requester.getRole();

        ensureRequesterCanChangeRole(requesterRole);

        Role newRole = parseRole(newRoleStr);

        User target = userRepository.findByEmail(targetUserEmail)
                .orElseThrow(EntityNotFoundException::new);

        ensureNotSelf(requester, target);

        if (requesterRole == Role.BIBLIOTHEEKBEHEERDER) {
            enforceLibrarianConstraints(requester, target, newRole);
        }

        target.setRole(newRole);
        userRepository.save(target);
        return userMapper.toUserResponse(target);
    }

    private void ensureRequesterCanChangeRole(Role requesterRole) {
        if (requesterRole != Role.BIBLIOTHEEKBEHEERDER && requesterRole != Role.ADMIN) {
            throw new SecurityException("Insufficient permissions");
        }
    }

    private Role parseRole(String newRoleStr) {
        try {
            return Role.valueOf(newRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRoleStr);
        }
    }

    private void ensureNotSelf(User requester, User target) {
        if (requester.getId().equals(target.getId())) {
            throw new SecurityException("Cannot change your own role");
        }
    }

     private void enforceLibrarianConstraints(User requester, User target, Role newRole) {
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

    public UserResponse changeUserSchool(String email, Long schoolId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(EntityNotFoundException::new);
        user.setSchool(school);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public UserResponse requestDeletion(Integer userId) {
        User user = findUserById(userId);
        if (user.getRole() != Role.LEERLING) {
            throw new SecurityException("Only students can request account deletion");
        }
        user.setDeletionRequestedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public UserResponse cancelDeletion(Integer userId) {
        User user = findUserById(userId);
        user.setDeletionRequestedAt(null);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }
}