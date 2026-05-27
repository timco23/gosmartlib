package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletionSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeletionScheduler deletionScheduler;

    private User expiredUser;
    private User recentUser;

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), name);
            throw e;
        }
    }

    private User buildUser(Integer id, String email, Role role, LocalDateTime deletionRequestedAt) {
        User u = new User();
        setField(u, "id", id);
        setField(u, "email", email);
        setField(u, "role", role);
        setField(u, "name", "Test");
        setField(u, "deletionRequestedAt", deletionRequestedAt);
        return u;
    }

    @BeforeEach
    void setUp() {
        expiredUser = buildUser(1, "expired@test.be", Role.LEERLING,
                LocalDateTime.now().minusHours(25));
        recentUser = buildUser(2, "recent@test.be", Role.LEERLING,
                LocalDateTime.now().minusHours(10));
    }

    @Test
    void scheduler_deletesUsersOlderThan24h() {
        when(userRepository.findByDeletionRequestedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredUser));
        deletionScheduler.deleteExpiredAccounts();
        verify(userRepository).deleteUserByIdNative(1);
    }

    @Test
    void scheduler_doesNotDeleteUsersWithin24h() {
        when(userRepository.findByDeletionRequestedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());
        deletionScheduler.deleteExpiredAccounts();
        verify(userRepository, never()).deleteUserByIdNative(any());
    }

    @Test
    void scheduler_doesNothingWhenNoUsersFound() {
        when(userRepository.findByDeletionRequestedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());
        deletionScheduler.deleteExpiredAccounts();
        verify(userRepository, never()).deleteUserByIdNative(any());
        verify(userRepository, never()).deleteAll(any());
    }

    @Test
    void scheduler_deletesMultipleExpiredUsers() {
        User anotherExpiredUser = buildUser(3, "expired2@test.be", Role.LEERLING,
                LocalDateTime.now().minusHours(30));
        when(userRepository.findByDeletionRequestedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredUser, anotherExpiredUser));
        deletionScheduler.deleteExpiredAccounts();
        verify(userRepository).deleteUserByIdNative(1);
        verify(userRepository).deleteUserByIdNative(3);
        verify(userRepository, times(2)).deleteUserByIdNative(any());
    }
}