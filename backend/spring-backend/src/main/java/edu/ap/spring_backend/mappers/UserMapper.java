package edu.ap.spring_backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.User;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "schoolName", expression = "java(user.getSchool() != null ? user.getSchool().getName() : null)")
    @Mapping(target = "schoolId", expression = "java(user.getSchool() != null ? user.getSchool().getId() : null)")
    @Mapping(target = "items_borrowed", source = "itemsBorrowed")
    UserResponse toUserResponse(User user);
}