package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.ClassDTO;
import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getOwnProfile_ok() throws Exception {
        when(userService.getOwnProfile(123)).thenReturn(user());

        mockMvc.perform(get("/user/profile").principal(principal("123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getProfileByEmail_ok() throws Exception {
        when(userService.getProfile("user@example.com")).thenReturn(user());

        mockMvc.perform(get("/user/profile/user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void getSchoolUsers_ok() throws Exception {
        when(userService.getUsersByCurrentSchool(123)).thenReturn(List.of(user()));

        mockMvc.perform(get("/user/school-users").principal(principal("123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getFavoriteBooks_ok() throws Exception {
        when(userService.getFavoriteBooks("user@example.com")).thenReturn(new ItemDTO[]{item()});

        mockMvc.perform(get("/user/favorites").param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void addFavoriteBook_ok() throws Exception {
        doNothing().when(userService).addFavoriteBook("user@example.com", 1L);

        mockMvc.perform(post("/user/favorites")
                        .param("email", "user@example.com")
                        .param("itemId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Book added")));
    }

    @Test
    void addFavoriteBook_badRequestOnError() throws Exception {
        doThrow(new RuntimeException("nope")).when(userService).addFavoriteBook("user@example.com", 1L);

        mockMvc.perform(post("/user/favorites")
                        .param("email", "user@example.com")
                        .param("itemId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("nope")));
    }

    @Test
    void deleteFavoriteBook_ok() throws Exception {
        doNothing().when(userService).deleteFavoriteBook("user@example.com", 1L);

        mockMvc.perform(delete("/user/favorites")
                        .param("email", "user@example.com")
                        .param("itemId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteFavoriteBook_badRequestOnError() throws Exception {
        doThrow(new RuntimeException("nope")).when(userService).deleteFavoriteBook("user@example.com", 1L);

        mockMvc.perform(delete("/user/favorites")
                        .param("email", "user@example.com")
                        .param("itemId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserRole_ok() throws Exception {
        when(userService.updateUserRole(123, "user@example.com", "ADMIN")).thenReturn(user());

        mockMvc.perform(put("/user/role")
                        .param("email", "user@example.com")
                        .param("role", "ADMIN")
                        .principal(principal("123")))
                .andExpect(status().isOk());
    }

    @Test
    void changeSchool_notFound() throws Exception {
        when(userService.changeUserSchool("user@example.com", 1L))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(put("/user/school")
                        .param("email", "user@example.com")
                        .param("schoolId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBorrowedItem_ok() throws Exception {
        when(userService.addToItemsBorrowed(1)).thenReturn(user());

        mockMvc.perform(put("/user/addBorrowed").param("userId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void addBorrowedItem_badRequestOnError() throws Exception {
        when(userService.addToItemsBorrowed(1)).thenThrow(new RuntimeException("nope"));

        mockMvc.perform(put("/user/addBorrowed").param("userId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeBorrowedItem_ok() throws Exception {
        when(userService.removeFromItemsBorrowed(1)).thenReturn(user());

        mockMvc.perform(put("/user/removeBorrowed").param("userId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void removeBorrowedItem_badRequestOnError() throws Exception {
        when(userService.removeFromItemsBorrowed(1)).thenThrow(new RuntimeException("nope"));

        mockMvc.perform(put("/user/removeBorrowed").param("userId", "1"))
                .andExpect(status().isBadRequest());
    }

    private Authentication principal(String name) {
        return new UsernamePasswordAuthenticationToken(name, "N/A");
    }

    private UserResponse user() {
        List<ClassDTO> classes = new ArrayList<>();
        classes.add(new ClassDTO(1L, "1A", 1L));
        return new UserResponse(
                1, "Doe", "Jane", "user@example.com", "LEERLING",
                "S123", classes, "AP", 1L, new ItemDTO[]{item()}, 2, null, "orid"
        );
    }

    private ItemDTO item() {
        return new ItemDTO(
                1L, "Dune", "Ace Books", "Frank Herbert", "9780441568529",
                1965, "ENGELS", 412, "https://covers.example.com/dune.jpg",
                4.5, 10, "A sci-fi epic.", false,
                List.of(new TagDTO(1L, "sci-fi")),
                List.of(1L), List.of(2L), 1L, List.of(3L),
                1L, false, false, null
        );
    }
}