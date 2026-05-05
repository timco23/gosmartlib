package edu.ap.spring_backend.mappers;

import edu.ap.spring_backend.dto.ClassDTO;
import edu.ap.spring_backend.jpa.SchoolClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SchoolClassMapper {
    @Mapping(target = "schoolId", expression = "java(schoolClass.getSchool() != null ? schoolClass.getSchool().getId() : null)")
    ClassDTO toClassDTO(SchoolClass schoolClass);
}