package edu.ap.spring_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ap.spring_backend.dto.DidacticCategoryDTO;
import edu.ap.spring_backend.jpa.DidacticCategory;
import edu.ap.spring_backend.service.DidacticCategoryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DidacticCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DidacticCategoryControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DidacticCategoryService didacticCategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private DidacticCategory buildCategory(Long id, String name) {
        DidacticCategory cat = new DidacticCategory(name);
        cat.setId(id);
        return cat;
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        Mockito.when(didacticCategoryService.getAll())
                .thenReturn(List.of(buildCategory(1L, "Wiskunde")));

        mockMvc.perform(get("/didacticcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Wiskunde"));
    }

    @Test
    void getAll_empty_shouldReturnEmptyList() throws Exception {
        Mockito.when(didacticCategoryService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/didacticcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addDidacticCategory_shouldReturnCreated() throws Exception {
        DidacticCategoryDTO dto = new DidacticCategoryDTO(null, "Wiskunde");
        Mockito.when(didacticCategoryService.addDidacticCategory(Mockito.any()))
                .thenReturn(buildCategory(1L, "Wiskunde"));

        mockMvc.perform(post("/didacticcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Wiskunde"));
    }

    @Test
    void updateDidacticCategory_shouldReturnUpdated() throws Exception {
        DidacticCategoryDTO dto = new DidacticCategoryDTO(1L, "Wetenschappen");
        Mockito.when(didacticCategoryService.updateDidacticCategory(Mockito.eq(1L), Mockito.any()))
                .thenReturn(buildCategory(1L, "Wetenschappen"));

        mockMvc.perform(put("/didacticcategories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Wetenschappen"));
    }

    @Test
    void deleteDidacticCategory_shouldReturn204() throws Exception {
        Mockito.doNothing().when(didacticCategoryService).deleteDidacticCategory(1L);

        mockMvc.perform(delete("/didacticcategories/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(didacticCategoryService).deleteDidacticCategory(1L);
    }
}