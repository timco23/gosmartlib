package edu.ap.spring_backend.integration;

import edu.ap.spring_backend.dao.ReviewRepository;
import edu.ap.spring_backend.jpa.Item;
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
class ReviewControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    private School school;
    private User student;
    private User otherStudent;
    private User librarian;
    private Item item;

    @BeforeEach
    void setUp() {
        school       = createSchool("Review School");
        student      = createUser("student@test.be",  Role.LEERLING,            school);
        otherStudent = createUser("other@test.be",    Role.LEERLING,            school);
        librarian    = createUser("lib@test.be",       Role.BIBLIOTHEEKBEHEERDER, school);
        item         = createItem("Dune", "Frank Herbert");
    }

    @Test
    void getReviews_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/books/" + item.getId() + "/reviews"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getReviews_authenticated_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getReviews_afterCreating_returnsReview() throws Exception {
        String body = """
                {"stars": 4, "description": "Geweldig boek!", "isAnonymous": false}
                """;
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stars", is(4)))
                .andExpect(jsonPath("$[0].description", is("Geweldig boek!")));
    }

    @Test
    void createReview_unauthenticated_returns401() throws Exception {
        String body = """
                {"stars": 3, "description": "test", "isAnonymous": false}
                """;
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createReview_validData_returns201AndUpdatesItemScore() throws Exception {
        String body = """
                {"stars": 5, "description": "Fantastisch!", "isAnonymous": false}
                """;

        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stars", is(5)))
                .andExpect(jsonPath("$.description", is("Fantastisch!")));

        Item updated = itemRepository.findById(item.getId()).orElseThrow();
        assert updated.getReviewCount() == 1;
        assert updated.getReviewScore() == 5.0;
    }

    @Test
    void createReview_duplicateBysaméUser_returns409() throws Exception {
        String body = """
                {"stars": 3, "description": "Eerste review", "isAnonymous": false}
                """;

        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void createReview_withProfanity_returns400() throws Exception {
        String body = """
                {"stars": 1, "description": "Dit is shit", "isAnonymous": false}
                """;

        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReview_averageScoreCalculatedCorrectly() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 5, \"description\": \"Top!\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(otherStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 3, \"description\": \"Goed\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        Item updated = itemRepository.findById(item.getId()).orElseThrow();
        assert updated.getReviewCount() == 2;
        assert updated.getReviewScore() == 4.0;
    }

    @Test
    void deleteReview_byOwner_returns204AndUpdatesScore() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 4, \"description\": \"Goed\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository.findByItemId(item.getId()).get(0).getId();

        mockMvc.perform(delete("/books/" + item.getId() + "/reviews/" + reviewId)
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isNoContent());

        Item updated = itemRepository.findById(item.getId()).orElseThrow();
        assert updated.getReviewCount() == 0;
        assert updated.getReviewScore() == 0.0;
    }

    @Test
    void deleteReview_byOtherStudent_returns403() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 4, \"description\": \"Goed\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository.findByItemId(item.getId()).get(0).getId();

        mockMvc.perform(delete("/books/" + item.getId() + "/reviews/" + reviewId)
                        .header("Authorization", jwtFor(otherStudent)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteReview_byLibrarian_returns204() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 2, \"description\": \"Matig\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository.findByItemId(item.getId()).get(0).getId();

        mockMvc.perform(delete("/books/" + item.getId() + "/reviews/" + reviewId)
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAllReviews_asStudent_returns403() throws Exception {
        mockMvc.perform(delete("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAllReviews_asLibrarian_returns204() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 4, \"description\": \"Goed\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(otherStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 2, \"description\": \"Matig\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(librarian)))
                .andExpect(status().isNoContent());

        assert reviewRepository.findByItemId(item.getId()).isEmpty();
    }

    @Test
    void reportReview_byOtherUser_returns201() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 1, \"description\": \"Slecht\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository.findByItemId(item.getId()).get(0).getId();

        mockMvc.perform(post("/books/" + item.getId() + "/reviews/" + reviewId + "/reports")
                        .header("Authorization", jwtFor(otherStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Ongepaste inhoud\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void reportReview_byAuthor_returns400() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 3, \"description\": \"Eigen review\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository.findByItemId(item.getId()).get(0).getId();

        mockMvc.perform(post("/books/" + item.getId() + "/reviews/" + reviewId + "/reports")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"test\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportReview_duplicate_returns409() throws Exception {
        mockMvc.perform(post("/books/" + item.getId() + "/reviews")
                        .header("Authorization", jwtFor(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 1, \"description\": \"Slecht\", \"isAnonymous\": false}"))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository.findByItemId(item.getId()).get(0).getId();

        mockMvc.perform(post("/books/" + item.getId() + "/reviews/" + reviewId + "/reports")
                        .header("Authorization", jwtFor(otherStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Spam\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books/" + item.getId() + "/reviews/" + reviewId + "/reports")
                        .header("Authorization", jwtFor(otherStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Spam nogmaals\"}"))
                .andExpect(status().isConflict());
    }
}