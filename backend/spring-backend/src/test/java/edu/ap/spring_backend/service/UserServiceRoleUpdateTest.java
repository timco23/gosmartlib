package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.mappers.ItemMapper;
import edu.ap.spring_backend.mappers.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceRoleUpdateTest {

    @Mock private UserRepository userRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private UserMapper userMapper;
    @Mock private ItemMapper itemMapper;

    @InjectMocks private UserService userService;

    @Test
    void updateUserRole_adminCanUpdate() {
        User requester = new User();
        ReflectionTestUtils.setField(requester, "id", 1);
        requester.setRole(Role.ADMIN);

        User target = new User();
        ReflectionTestUtils.setField(target, "id", 2);
        target.setRole(Role.LEERLING);

        when(userRepository.findById(1)).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail("target@school.be")).thenReturn(Optional.of(target));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(mock(UserResponse.class));

        UserResponse res = userService.updateUserRole(1, "target@school.be", "LEERKRACHT");

        assertNotNull(res);
        assertEquals(Role.LEERKRACHT, target.getRole());
        verify(userRepository).save(target);
    }

    @Test
    void updateUserRole_librarianCannotAssignAdmin() {
        User requester = new User();
        ReflectionTestUtils.setField(requester, "id", 1);
        requester.setRole(Role.BIBLIOTHEEKBEHEERDER);

        User target = new User();
        ReflectionTestUtils.setField(target, "id", 2);
        target.setRole(Role.LEERLING);

        when(userRepository.findById(1)).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail("target@school.be")).thenReturn(Optional.of(target));

        assertThrows(SecurityException.class,
                () -> userService.updateUserRole(1, "target@school.be", "ADMIN"));
    }

    @Test
    void updateUserRole_librarianDifferentSchool_throws() {
        User requester = new User();
        ReflectionTestUtils.setField(requester, "id", 1);
        requester.setRole(Role.BIBLIOTHEEKBEHEERDER);
        School s1 = new School();
        s1.setId(1L);
        requester.setSchool(s1);

        User target = new User();
        ReflectionTestUtils.setField(target, "id", 2);
        target.setRole(Role.LEERLING);
        School s2 = new School();
        s2.setId(2L);
        target.setSchool(s2);

        when(userRepository.findById(1)).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail("target@school.be")).thenReturn(Optional.of(target));

        assertThrows(SecurityException.class,
                () -> userService.updateUserRole(1, "target@school.be", "LEERKRACHT"));
    }
}