package edu.ap.spring_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ap.spring_backend.dto.GenreWithSubgenresDTO;
import edu.ap.spring_backend.jpa.Genre;
import edu.ap.spring_backend.service.GenreService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(GenreController.class)
@AutoConfigureMockMvc(addFilters = false)
class GenreControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllGenres_shouldReturnList() throws Exception {
        GenreWithSubgenresDTO dto = new GenreWithSubgenresDTO(1L, "Rock", List.of());

        Mockito.when(genreService.getAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rock"));
    }

    @Test
    void addGenre_shouldReturnSavedGenre() throws Exception {
        Genre genre = new Genre("Jazz");
        genre.setId(1L);

        Mockito.when(genreService.addGenre(Mockito.any())).thenReturn(genre);

        mockMvc.perform(post("/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(genre)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jazz"));
    }

    @Test
    void updateGenre_shouldReturnUpdatedGenre() throws Exception {
        Genre updated = new Genre("Pop");
        updated.setId(1L);

        Mockito.when(genreService.updateGenre(Mockito.eq(1L), Mockito.any()))
                .thenReturn(updated);

        mockMvc.perform(put("/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pop"));
    }

    @Test
    void deleteGenre_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/genres/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(genreService).deleteGenre(1L);
    }
}