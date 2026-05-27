package edu.ap.spring_backend.mappers;

import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapperImpl(); // geen argumenten nodig
    }

    @Test
    void toUserResponse_mapsRoleAsString() {
        User user = new User();
        user.setRole(Role.ADMIN);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void toUserResponse_mapsNameAndEmail() {
        User user = new User();
        user.setRole(Role.LEERLING);
        user.setName("Janssen");
        user.setFirstName("Jan");
        user.setEmail("jan@test.be");

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getName()).isEqualTo("Janssen");
        assertThat(response.getFirstName()).isEqualTo("Jan");
        assertThat(response.getEmail()).isEqualTo("jan@test.be");
    }

    @Test
    void toUserResponse_mapsSchoolNameAndId() {
        School school = new School();
        school.setId(5L);
        school.setName("AP Hogeschool");

        User user = new User();
        user.setRole(Role.LEERKRACHT);
        user.setSchool(school);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getSchoolName()).isEqualTo("AP Hogeschool");
        assertThat(response.getSchoolId()).isEqualTo(5L);
    }

    @Test
    void toUserResponse_nullSchool_returnsNullSchoolFields() {
        User user = new User();
        user.setRole(Role.ADMIN);
        user.setSchool(null);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getSchoolName()).isNull();
        assertThat(response.getSchoolId()).isNull();
    }

    @Test
    void toUserResponse_mapsDeletionRequestedAt() {
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setRole(Role.LEERLING);
        user.setDeletionRequestedAt(now);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getDeletionRequestedAt()).isEqualTo(now);
    }

    @Test
    void toUserResponse_nullDeletionRequestedAt() {
        User user = new User();
        user.setRole(Role.ADMIN);
        user.setDeletionRequestedAt(null);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getDeletionRequestedAt()).isNull();
    }

    @Test
    void toUserResponse_mapsItemsBorrowed() {
        User user = new User();
        user.setRole(Role.LEERLING);
        user.setItemsBorrowed(3);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getItemsBorrowed()).isEqualTo(3);
    }
}