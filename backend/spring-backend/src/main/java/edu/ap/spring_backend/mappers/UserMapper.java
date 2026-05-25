package edu.ap.spring_backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.User;

@Mapper(componentModel = "spring", uses = {ItemMapper.class, SchoolClassMapper.class})
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "schoolName", expression = "java(user.getSchool() != null ? user.getSchool().getName() : null)")
    @Mapping(target = "schoolId", expression = "java(user.getSchool() != null ? user.getSchool().getId() : null)")
    @Mapping(target = "itemsBorrowed", source = "itemsBorrowed")
    @Mapping(target = "deletionRequestedAt", source = "deletionRequestedAt")
    UserResponse toUserResponse(User user);
}