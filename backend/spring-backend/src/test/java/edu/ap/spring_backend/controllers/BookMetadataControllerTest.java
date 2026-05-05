package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.service.bookmetadata.BookMetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookMetadataController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookMetadataControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookMetadataService bookMetadataService;

    @Test
    void getByIsbn_found() throws Exception {
        BookMetadata meta = new BookMetadata(
                "Dune", "Frank Herbert", "A sci-fi epic.", "ENGELS",
                "Ace Books", 412, 1965, "https://covers.example.com/dune.jpg"
        );

        when(bookMetadataService.fetchByIsbn("9780441568529"))
                .thenReturn(Optional.of(meta));

        mockMvc.perform(get("/books/metadata").param("isbn", "9780441568529"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.language").value("ENGELS"));
    }

    @Test
    void getByIsbn_notFound() throws Exception {
        when(bookMetadataService.fetchByIsbn("9780441568529"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/books/metadata").param("isbn", "9780441568529"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIsbn_invalid_returns400() throws Exception {
        mockMvc.perform(get("/books/metadata").param("isbn", "123"))
                .andExpect(status().isBadRequest());

        verify(bookMetadataService, never()).fetchByIsbn(anyString());
    }
}