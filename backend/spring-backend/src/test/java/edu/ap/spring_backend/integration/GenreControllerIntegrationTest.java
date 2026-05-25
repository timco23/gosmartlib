package edu.ap.spring_backend.integration;

import edu.ap.spring_backend.dao.GenreRepository;
import edu.ap.spring_backend.jpa.Genre;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
class GenreControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GenreRepository genreRepository;

    private School school;
    private User student;
    private User librarian;
    private User admin;
    private Genre testGenre;

    @BeforeEach
    void setUp() {
        school    = createSchool("Integratie School");
        student   = createUser("student@test.be",   Role.LEERLING,            school);
        librarian = createUser("lib@test.be",        Role.BIBLIOTHEEKBEHEERDER, school);
        admin     = createUser("admin@test.be",      Role.ADMIN,               school);

        testGenre = new Genre();
        testGenre.setName("Integratie Genre");
        testGenre = genreRepository.save(testGenre);
    }

    @Test
    void getAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAll_asStudent_returns200WithGenres() throws Exception {
        mockMvc.perform(get("/genres")
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void getAll_asLibrarian_returns200() throws Exception {
        mockMvc.perform(get("/genres")
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isOk());
    }

    @Test
    void addGenre_asStudent_returns403() throws Exception {
        String body = """
                {"name": "Nieuw genre"}
                """;

        mockMvc.perform(post("/genres")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void addGenre_asLibrarian_returns200AndPersists() throws Exception {
        String body = """
                {"name": "SciFi Integratie"}
                """;

        mockMvc.perform(post("/genres")
                        .header("Authorization", jwtFor(librarian))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("SciFi Integratie")));
    }

    @Test
    void addGenre_asAdmin_returns200() throws Exception {
        String body = """
                {"name": "Admin Genre"}
                """;

        mockMvc.perform(post("/genres")
                        .header("Authorization", jwtFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Admin Genre")));
    }

    @Test
    void addGenre_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"X\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateGenre_asLibrarian_returns200AndUpdates() throws Exception {
        String body = String.format("{\"name\": \"Bijgewerkt Genre\"}");

        mockMvc.perform(put("/genres/" + testGenre.getId())
                        .header("Authorization", jwtFor(librarian))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Bijgewerkt Genre")));
    }

    @Test
    void updateGenre_asStudent_returns403() throws Exception {
        mockMvc.perform(put("/genres/" + testGenre.getId())
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Verboden\"}"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void deleteGenre_asLibrarian_returns204AndRemovesFromDb() throws Exception {
        Genre toDelete = new Genre();
        toDelete.setName("Te verwijderen");
        toDelete = genreRepository.save(toDelete);

        mockMvc.perform(delete("/genres/" + toDelete.getId())
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isNoContent());

        assert genreRepository.findById(toDelete.getId()).isEmpty();
    }

    @Test
    void deleteGenre_asStudent_returns403() throws Exception {
        mockMvc.perform(delete("/genres/" + testGenre.getId())
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteGenre_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/genres/" + testGenre.getId()))
                .andExpect(status().is4xxClientError());
    }
}