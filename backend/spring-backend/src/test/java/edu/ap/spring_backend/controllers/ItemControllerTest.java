package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.ItemRequestDTO;
import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.service.BarcodeService;
import edu.ap.spring_backend.service.BookCoverCacheService;
import edu.ap.spring_backend.service.BookCsvImportService;
import edu.ap.spring_backend.service.ItemService;
import edu.ap.spring_backend.service.SchoolItemCopyService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private SchoolItemCopyService schoolItemCopyService;

    @MockBean
    private BookCsvImportService bookCsvImportService;

    @MockBean
    private BarcodeService barcodeService;

    @MockBean
    private BookCoverCacheService bookCoverCacheService;

    @Test
    void getAllItems_returnsList() throws Exception {
        when(itemService.getAllItems()).thenReturn(List.of(item()));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getItemById_returnsItem() throws Exception {
        when(itemService.getItemById(1L)).thenReturn(item());

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void getItemByTitle_returnsItem() throws Exception {
        when(itemService.getItemByTitle("Dune")).thenReturn(item());

        mockMvc.perform(get("/books/search").param("title", "Dune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void getFeaturedItems_returnsList() throws Exception {
        when(itemService.getFeaturedItems(1L)).thenReturn(List.of(item()));

        mockMvc.perform(get("/books/featured/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getLatestItems_returnsList() throws Exception {
        when(itemService.get10LatestItems(1L)).thenReturn(List.of(item()));

        mockMvc.perform(get("/books/latest/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getFilteredItems_student_forcesDidacticFalse() throws Exception {
        when(itemService.getFilteredItems(any())).thenReturn(null);

        var auth = new UsernamePasswordAuthenticationToken(
                "123", "N/A", List.of(new SimpleGrantedAuthority("ROLE_LEERLING"))
        );

        mockMvc.perform(get("/filteredBooks")
                        .param("schoolId", "1")
                        .param("schoolSpecificBooks", "true")
                        .param("didactic", "true")
                        .param("didacticCategoryIds", "1")
                        .principal(auth))
                .andExpect(status().isOk());

        ArgumentCaptor<ItemRequestDTO> captor = ArgumentCaptor.forClass(ItemRequestDTO.class);
        verify(itemService).getFilteredItems(captor.capture());

        Object req = captor.getValue();
        Boolean didactic = (Boolean) readAccessor(req, "didactic", "getDidactic", "isDidactic");
        Object didacticCategoryIds = readAccessor(req, "didacticCategoryIds", "getDidacticCategoryIds");

        assertEquals(Boolean.FALSE, didactic);
        assertNull(didacticCategoryIds);
    }

    @Test
    void getBookCover_fallbackOnError() throws Exception {
        when(bookCoverCacheService.getByItemId(eq(1L), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/books/1/cover"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"))
                .andExpect(content().string(containsString("<svg")));
    }

    private ItemDTO item() {
        return new ItemDTO(
                1L, "Dune", "Ace Books", "Frank Herbert", "9780441568529",
                1965, "ENGELS", 412, "https://covers.example.com/dune.jpg",
                4.5, 10, "A sci-fi epic.",
                List.of(new TagDTO(1L, "sci-fi")),
                List.of(1L), List.of(2L), 1L, List.of(3L),
                1L, false, false, null
        );
    }

    private static Object readAccessor(Object target, String... names) {
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name);
                m.setAccessible(true);
                return m.invoke(target);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}