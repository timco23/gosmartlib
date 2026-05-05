package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private SchoolRepository schoolRepository;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User beheerderUser;
    private User leerlingUser;
    private School school;
    private Item item;


    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Kon veld '" + fieldName + "' niet instellen op " + target.getClass().getSimpleName(), e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), name);
            throw e;
        }
    }

    // ── help: make a School with Lombok setters 

    private School buildSchool(Long id, String name) {
        School s = new School();
        s.setId(id);           // Lombok setter
        s.setName(name);
        return s;
    }

    // ── help: make a User through reflection

    private User buildUser(Integer id, String name, String firstName,
                           String email, Role role, School school) {
        User u = new User();
        setField(u, "id", id);
        setField(u, "name", name);
        setField(u, "firstName", firstName);
        setField(u, "email", email);
        setField(u, "role", role);
        setField(u, "school", school);
        setField(u, "favoriteBooks", new ArrayList<>());
        setField(u, "itemsBorrowed", 0);
        return u;
    }

    // ── help: make an Item with Lombok setters

    private Item buildItem(Long id, String title) {
        Item i = new Item();
        i.setId(id);
        i.setTitle(title);
        i.setAuthor("Auteur");
        i.setBookLanguage("nl");
        i.setTags(new HashSet<>());
        i.setGenres(new HashSet<>());
        i.setSubgenres(new HashSet<>());
        i.setDidacticCategories(new HashSet<>());
        return i;
    }

    @BeforeEach
    void setUp() {
        school = buildSchool(1L, "Testschool");

        adminUser    = buildUser(1, "Admin",    "Ad", "admin@test.be",    Role.ADMIN,                school);
        beheerderUser= buildUser(2, "Beheerder","Be", "beheerder@test.be",Role.BIBLIOTHEEKBEHEERDER, school);
        leerlingUser = buildUser(3, "Leerling", "Le", "leerling@test.be", Role.LEERLING,             school);

        item = buildItem(10L, "Test Boek");
    }

    @Test
    void getOwnProfile_returnsCorrectUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        UserResponse response = userService.getOwnProfile(1);

        assertThat(response.getEmail()).isEqualTo("admin@test.be");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getSchoolId()).isEqualTo(1L);
    }

    @Test
    void getOwnProfile_throwsWhenUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOwnProfile(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getProfile_returnsCorrectUser() {
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));

        UserResponse response = userService.getProfile("leerling@test.be");

        assertThat(response.getName()).isEqualTo("Leerling");
        assertThat(response.getFirstName()).isEqualTo("Le");
        assertThat(response.getSchoolName()).isEqualTo("Testschool");
    }

    @Test
    void getProfile_throwsWhenEmailNotFound() {
        when(userRepository.findByEmail("onbekend@test.be")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("onbekend@test.be"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getFavoriteBooks_returnsEmptyArrayWhenNoFavorites() {
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));

        ItemDTO[] result = userService.getFavoriteBooks("leerling@test.be");

        assertThat(result).isEmpty();
    }

    @Test
    void getFavoriteBooks_returnsMappedItems() {
        leerlingUser.getFavoriteBooks().add(item);
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));

        ItemDTO[] result = userService.getFavoriteBooks("leerling@test.be");

        assertThat(result).hasSize(1);
        assertThat(result[0].title()).isEqualTo("Test Boek"); // record accessor
    }

    @Test
    void getFavoriteBooks_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("niemand@test.be")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getFavoriteBooks("niemand@test.be"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addFavoriteBook_addsItemToFavorites() {
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        userService.addFavoriteBook("leerling@test.be", 10L);

        assertThat(leerlingUser.getFavoriteBooks()).contains(item);
        verify(userRepository).save(leerlingUser);
    }

    @Test
    void addFavoriteBook_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("niemand@test.be")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addFavoriteBook("niemand@test.be", 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addFavoriteBook_throwsWhenItemNotFound() {
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addFavoriteBook("leerling@test.be", 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void deleteFavoriteBook_removesItemFromFavorites() {
        leerlingUser.getFavoriteBooks().add(item);
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        userService.deleteFavoriteBook("leerling@test.be", 10L);

        assertThat(leerlingUser.getFavoriteBooks()).doesNotContain(item);
        verify(userRepository).save(leerlingUser);
    }

    @Test
    void deleteFavoriteBook_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("niemand@test.be")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteFavoriteBook("niemand@test.be", 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteFavoriteBook_throwsWhenItemNotFound() {
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteFavoriteBook("leerling@test.be", 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void getUsersByCurrentSchool_returnsUsersFromSameSchool() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findBySchoolId(1L)).thenReturn(List.of(adminUser, leerlingUser));

        List<UserResponse> result = userService.getUsersByCurrentSchool(1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserResponse::getEmail)
                .containsExactlyInAnyOrder("admin@test.be", "leerling@test.be");
    }

    @Test
    void getUsersByCurrentSchool_returnsEmptyListWhenUserHasNoSchool() {
        setField(adminUser, "school", null);
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        List<UserResponse> result = userService.getUsersByCurrentSchool(1);

        assertThat(result).isEmpty();
        verify(userRepository, never()).findBySchoolId(any());
    }

    @Test
    void getUsersByCurrentSchool_throwsWhenUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUsersByCurrentSchool(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addToItemsBorrowed_incrementsCounter() {
        when(userRepository.findById(3)).thenReturn(Optional.of(leerlingUser));

        UserResponse response = userService.addToItemsBorrowed(3);

        assertThat(response.getItems_borrowed()).isEqualTo(1);
        verify(userRepository).save(leerlingUser);
    }

    @Test
    void addToItemsBorrowed_throwsWhenUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addToItemsBorrowed(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void removeFromItemsBorrowed_decrementsCounter() {
        setField(leerlingUser, "itemsBorrowed", 2);
        when(userRepository.findById(3)).thenReturn(Optional.of(leerlingUser));

        UserResponse response = userService.removeFromItemsBorrowed(3);

        assertThat(response.getItems_borrowed()).isEqualTo(1);
    }

    @Test
    void removeFromItemsBorrowed_doesNotGoBelowZero() {
        when(userRepository.findById(3)).thenReturn(Optional.of(leerlingUser));

        UserResponse response = userService.removeFromItemsBorrowed(3);

        assertThat(response.getItems_borrowed()).isEqualTo(0);
    }

    @Test
    void removeFromItemsBorrowed_throwsWhenUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeFromItemsBorrowed(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUserRole_adminCanChangeAnyRole() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));

        UserResponse response = userService.updateUserRole(1, "leerling@test.be", "LEERKRACHT");

        assertThat(response.getRole()).isEqualTo("LEERKRACHT");
        verify(userRepository).save(leerlingUser);
    }

    @Test
    void updateUserRole_beheerderCanChangeRoleWithinSameSchool() {
        when(userRepository.findById(2)).thenReturn(Optional.of(beheerderUser));
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));

        UserResponse response = userService.updateUserRole(2, "leerling@test.be", "LEERKRACHT");

        assertThat(response.getRole()).isEqualTo("LEERKRACHT");
        verify(userRepository).save(leerlingUser);
    }

    @Test
    void updateUserRole_beheerderCannotAssignAdminRole() {
        when(userRepository.findById(2)).thenReturn(Optional.of(beheerderUser));
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));

        assertThatThrownBy(() -> userService.updateUserRole(2, "leerling@test.be", "ADMIN"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("cannot assign ADMIN");
    }

    @Test
    void updateUserRole_beheerderCannotChangeAdminUser() {
        when(userRepository.findById(2)).thenReturn(Optional.of(beheerderUser));
        when(userRepository.findByEmail("admin@test.be")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.updateUserRole(2, "admin@test.be", "LEERLING"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("cannot change the role of an admin");
    }

    @Test
    void updateUserRole_throwsWhenChangingOwnRole() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail("admin@test.be")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.updateUserRole(1, "admin@test.be", "LEERLING"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("own role");
    }

    @Test
    void updateUserRole_throwsWhenRequesterHasInsufficientPermissions() {
        when(userRepository.findById(3)).thenReturn(Optional.of(leerlingUser));

        assertThatThrownBy(() -> userService.updateUserRole(3, "admin@test.be", "LEERLING"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Insufficient permissions");
    }

    @Test
    void updateUserRole_beheerderCannotChangeUserFromOtherSchool() {
        School otherSchool = buildSchool(99L, "Andere School");
        User otherUser = buildUser(10, "Andere", "An", "other@test.be", Role.LEERLING, otherSchool);

        when(userRepository.findById(2)).thenReturn(Optional.of(beheerderUser));
        when(userRepository.findByEmail("other@test.be")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> userService.updateUserRole(2, "other@test.be", "LEERKRACHT"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("same school");
    }

    @Test
    void updateUserRole_throwsOnInvalidRoleString() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.updateUserRole(1, "leerling@test.be", "ONGELDIG_ROL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void updateUserRole_throwsWhenTargetUserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail("niemand@test.be")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(1, "niemand@test.be", "LEERLING"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void changeUserSchool_updatesSchoolCorrectly() {
        School newSchool = buildSchool(2L, "Nieuwe School");
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(newSchool));

        UserResponse response = userService.changeUserSchool("leerling@test.be", 2L);

        assertThat(response.getSchoolName()).isEqualTo("Nieuwe School");
        assertThat(response.getSchoolId()).isEqualTo(2L);
        verify(userRepository).save(leerlingUser);
    }

    @Test
    void changeUserSchool_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("niemand@test.be")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeUserSchool("niemand@test.be", 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void changeUserSchool_throwsWhenSchoolNotFound() {
        when(userRepository.findByEmail("leerling@test.be")).thenReturn(Optional.of(leerlingUser));
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeUserSchool("leerling@test.be", 999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}