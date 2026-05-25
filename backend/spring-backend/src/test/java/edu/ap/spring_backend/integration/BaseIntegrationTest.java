package edu.ap.spring_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected SchoolRepository schoolRepository;

    @Autowired
    protected JwtService jwtService;

    protected School createSchool(String name) {
        School school = new School(
                name, "Teststraat 1",
                14, 7, 2, 5, 10,
                3, 1, 5,
                false, true, true,
                true, false);
        return schoolRepository.save(school);
    }

    protected User createUser(String email, Role role, School school) {
        User user = new User();
        user.setName("Test");
        user.setFirstName("User");
        user.setEmail(email);
        user.setPassword("{noop}password");
        user.setRole(role);
        user.setItemsBorrowed(0);
        if (school != null) user.setSchool(school);
        return userRepository.save(user);
    }

    protected Item createItem(String title, String author) {
        Item item = new Item();
        item.setTitle(title);
        item.setAuthor(author);
        item.setPublisher("Testuitgeverij");
        item.setIsbn("9781234567890");
        item.setReleaseYear(2020);
        item.setBookLanguage("Nederlands");
        item.setPageCount(200);
        item.setReviewScore(0.0);
        item.setReviewCount(0);
        item.setDescription("Testbeschrijving");
        return itemRepository.save(item);
    }

    protected String jwtFor(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}