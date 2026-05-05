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
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
                .orElseThrow(() -> new RuntimeException("Item not found"));
        user.getFavoriteBooks().add(item);
        userRepository.save(user);
    }

    public void deleteFavoriteBook(String email, Long itemId) {
        User user = findUserByEmail(email);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        user.getFavoriteBooks().remove(item);
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
        return userMapper.toUserResponse(target);
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
}