package edu.ap.spring_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ap.spring_backend.dto.LendingDTO;
import edu.ap.spring_backend.dto.LendingRequestDTO;
import edu.ap.spring_backend.service.LendingService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LendingController.class)
@AutoConfigureMockMvc(addFilters = false)
class LendingControllerTest extends BaseControllerTest  {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LendingService lendingService;

    @Autowired
    private ObjectMapper objectMapper;

    private LendingDTO buildDTO() {
        return new LendingDTO(
                1L,
                "Ada Lovelace",
                1L,
                "Dune",
                "Frank Herbert",
                9L,
                1L,
                "2024-01-01",
                "2024-01-10",
                null,
                false,
                "BC001",
                false
        );
    }

    @Test
    void getAllLendings_shouldReturnList() throws Exception {
        Mockito.when(lendingService.getAll()).thenReturn(List.of(buildDTO()));

        mockMvc.perform(get("/lendings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookTitle").value("Dune"));
    }

    @Test
    void getAllLendingsByUser_shouldReturnList() throws Exception {
        Mockito.when(lendingService.getAllByUser("user@example.com"))
                .thenReturn(List.of(buildDTO()));

        mockMvc.perform(get("/lendings/user")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].borrowerName").value("Ada Lovelace"));
    }

    @Test
    void getLendingById_shouldReturnDto() throws Exception {
        Mockito.when(lendingService.getById(1L)).thenReturn(buildDTO());

        mockMvc.perform(get("/lendings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value("Dune"));
    }

    @Test
    void addLending_shouldReturnCreatedDto() throws Exception {
        LendingRequestDTO request = new LendingRequestDTO(
                1,
                "BC001",
                1L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 10)
        );

        Mockito.when(lendingService.addLending(Mockito.any()))
                .thenReturn(buildDTO());

        mockMvc.perform(post("/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value("Dune"));
    }

    @Test
    void deleteLending_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/lendings/1"))
                .andExpect(status().isOk());

        Mockito.verify(lendingService).deleteLending(1L);
    }

    @Test
    void updateLending_shouldReturnUpdatedDto() throws Exception {
        LendingDTO dto = buildDTO();

        Mockito.when(lendingService.updateLending(Mockito.eq(1L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(put("/lendings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value("Dune"));
    }

    @Test
    void extendLending_shouldReturnExtendedDto() throws Exception {
        LendingDTO extended = new LendingDTO(
                1L,
                "Ada Lovelace",
                1L,
                "Dune",
                "Frank Herbert",
                9L,
                1L,
                "2024-01-01",
                "2024-01-17",
                null,
                false,
                "BC001",
                true
        );

        Mockito.when(lendingService.extendLending(1L)).thenReturn(extended);

        mockMvc.perform(post("/lendings/1/extend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extended").value(true));
    }

    @Test
    void getLendingsByBookId_shouldReturnList() throws Exception {
        Mockito.when(lendingService.getLendingsByBookId(9L))
                .thenReturn(List.of(buildDTO()));

        mockMvc.perform(get("/lendings/book/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId").value(9L));
    }
}