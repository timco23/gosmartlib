package edu.ap.spring_backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.jpa.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "tags", expression = "java(item.getTags().stream().map(tag -> new TagDTO((long) tag.getId(), tag.getName())).toList())")
    @Mapping(target = "genres", expression = "java(item.getGenres() != null ? item.getGenres().stream().map(g -> (long) g.getId()).toList() : java.util.List.of())")
    @Mapping(target = "subgenres", expression = "java(item.getSubgenres() != null ? item.getSubgenres().stream().map(s -> (long) s.getId()).toList() : java.util.List.of())")
    @Mapping(target = "readingLevel", expression = "java(item.getReadingLevel() != null ? (long) item.getReadingLevel().getId() : null)")
    @Mapping(target = "didacticCategories", expression = "java(item.getDidacticCategories() != null ? item.getDidacticCategories().stream().map(d -> (long) d.getId()).toList() : java.util.List.of())")
    @Mapping(target = "schoolId", expression = "java(null)")
    @Mapping(target = "featured", constant = "false")
    @Mapping(target = "campusId", expression = "java(null)")
    @Mapping(target = "isDidactic", source = "didactic")
    ItemDTO toItemDTO(Item item);
}