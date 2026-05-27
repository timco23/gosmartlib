package edu.ap.spring_backend.mappers;

import edu.ap.spring_backend.dto.ClassDTO;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolClassMapperTest {

    private SchoolClassMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SchoolClassMapperImpl();
    }

    @Test
    void toClassDTO_mapsSchoolId() {
        School school = new School();
        school.setId(10L);

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setClassName("6A");
        schoolClass.setSchool(school);

        ClassDTO dto = mapper.toClassDTO(schoolClass);

        assertThat(dto.schoolId()).isEqualTo(10L);
        assertThat(dto.className()).isEqualTo("6A");
    }

    @Test
    void toClassDTO_nullSchool_returnsNullSchoolId() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setClassName("5B");
        schoolClass.setSchool(null);

        ClassDTO dto = mapper.toClassDTO(schoolClass);

        assertThat(dto.schoolId()).isNull();
    }

    @Test
    void toClassDTO_mapsClassName() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setClassName("4C");
        schoolClass.setSchool(null);

        ClassDTO dto = mapper.toClassDTO(schoolClass);

        assertThat(dto.className()).isEqualTo("4C");
    }
}