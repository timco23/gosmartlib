package edu.ap.spring_backend.integration;

import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class LendingControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LendingRepository lendingRepository;

    @Autowired
    private SchoolItemCopyRepository copyRepository;

    private School school;
    private User student;
    private User librarian;
    private Item item;
    private SchoolItemCopy copy;
    private Lending existingLending;

    @BeforeEach
    void setUp() {
        school    = createSchool("Lending School");
        student   = createUser("student@test.be", Role.LEERLING,            school);
        librarian = createUser("lib@test.be",      Role.BIBLIOTHEEKBEHEERDER, school);
        item      = createItem("1984", "George Orwell");

        copy = new SchoolItemCopy();
        copy.setItem(item);
        copy.setSchool(school);
        copy.setBarcode("BC000099");
        copy.setCopy_status(CopyStatus.BESCHIKBAAR);
        copy.setCondition(ItemCondition.GOED);
        copy.setFeatured(false);
        copy = copyRepository.save(copy);

        existingLending = new Lending();
        existingLending.setBorrower(student);
        existingLending.setBook(copy);
        existingLending.setSchool(school);
        existingLending.setLendingDate(LocalDate.now().minusDays(7));
        existingLending.setReturnDate(LocalDate.now().plusDays(7));
        existingLending.setReturned(false);
        existingLending.setExtended(false);
        existingLending.setSnapSchoolYear("2025-2026");
        existingLending = lendingRepository.save(existingLending);
    }

    @Test
    void getAllLendings_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/lendings"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllLendings_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/lendings")
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllLendings_asLibrarian_returns200WithList() throws Exception {
        mockMvc.perform(get("/lendings")
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[*].bookTitle", hasItem("1984")));
    }

    @Test
    void getLendingsByUser_asStudent_returns200() throws Exception {
        mockMvc.perform(get("/lendings/user")
                        .header("Authorization", jwtFor(student))
                        .param("email", student.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].bookTitle", is("1984")));
    }

    @Test
    void getLendingsByUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/lendings/user").param("email", "x@x.be"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getLendingById_asLibrarian_returns200() throws Exception {
        mockMvc.perform(get("/lendings/" + existingLending.getId())
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingLending.getId().intValue())))
                .andExpect(jsonPath("$.bookTitle", is("1984")));
    }

    @Test
    void getLendingById_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/lendings/" + existingLending.getId())
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addLending_asLibrarian_returns201AndPersists() throws Exception {
        SchoolItemCopy copy2 = new SchoolItemCopy();
        copy2.setItem(item);
        copy2.setSchool(school);
        copy2.setBarcode("BC000098");
        copy2.setCopy_status(CopyStatus.BESCHIKBAAR);
        copy2.setCondition(ItemCondition.NIEUW);
        copy2.setFeatured(false);
        copy2 = copyRepository.save(copy2);

        String body = String.format("""
                {
                  "borrowerId": %d,
                  "copyBarcode": "BC000098",
                  "schoolId": %d,
                  "lendingDate": "%s",
                  "returnDate": "%s"
                }
                """,
                student.getId(),
                school.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(14));

        mockMvc.perform(post("/lendings")
                        .header("Authorization", jwtFor(librarian))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookTitle", is("1984")))
                .andExpect(jsonPath("$.returned", is(false)));
    }

    @Test
    void addLending_asStudent_returns403() throws Exception {
        mockMvc.perform(post("/lendings")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void extendLending_asLibrarian_returns200AndExtendsDate() throws Exception {
        LocalDate originalReturnDate = existingLending.getReturnDate();

        mockMvc.perform(post("/lendings/" + existingLending.getId() + "/extend")
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extendCount", is(1)));

        Lending updated = lendingRepository.findById(existingLending.getId()).orElseThrow();
        assert updated.getReturnDate().isAfter(originalReturnDate);
    }

    @Test
    void extendLending_whenMaxReached_returns400() throws Exception {
        existingLending.setExtendCount(2);
        lendingRepository.save(existingLending);

        mockMvc.perform(post("/lendings/" + existingLending.getId() + "/extend")
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLending_markReturned_asLibrarian_returns200() throws Exception {
        String body = String.format("""
                {
                  "returned": true,
                  "dateReturned": "%sT00:00:00"
                }
                """, LocalDate.now());

        mockMvc.perform(put("/lendings/" + existingLending.getId())
                        .header("Authorization", jwtFor(librarian))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returned", is(true)));

        Lending updated = lendingRepository.findById(existingLending.getId()).orElseThrow();
        assert updated.isReturned();
    }

    @Test
    void deleteLending_asLibrarian_returns204AndRemovesFromDb() throws Exception {
        mockMvc.perform(delete("/lendings/" + existingLending.getId())
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isNoContent());

        assert lendingRepository.findById(existingLending.getId()).isEmpty();
    }

    @Test
    void deleteLending_asStudent_returns403() throws Exception {
        mockMvc.perform(delete("/lendings/" + existingLending.getId())
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMostBorrowedThisMonth_asStudent_returns200OrNoContent() throws Exception {
        mockMvc.perform(get("/lendings/most-borrowed-this-month/" + school.getId())
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().is2xxSuccessful());
    }
}