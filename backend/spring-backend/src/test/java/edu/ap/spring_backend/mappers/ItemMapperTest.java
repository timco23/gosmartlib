package edu.ap.spring_backend.mappers;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private ItemMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ItemMapperImpl();
    }

    @Test
    void toItemDTO_mapsBasicFields() {
        Item item = new Item();
        item.setTitle("Test Book");
        item.setAuthor("Jan Janssen");
        item.setIsbn("1234567890");
        item.setTags(new HashSet<>());

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.title()).isEqualTo("Test Book");
        assertThat(dto.author()).isEqualTo("Jan Janssen");
        assertThat(dto.isbn()).isEqualTo("1234567890");
    }

    @Test
    void toItemDTO_schoolIdAndCampusIdAreAlwaysNull() {
        Item item = new Item();
        item.setTags(new HashSet<>());

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.schoolId()).isNull();
        assertThat(dto.campusId()).isNull();
    }

    @Test
    void toItemDTO_featuredIsAlwaysFalse() {
        Item item = new Item();
        item.setTags(new HashSet<>());

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.featured()).isFalse();
    }

    @Test
    void toItemDTO_mapsTags() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Thriller");

        Item item = new Item();
        item.setTags(Set.of(tag));

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.tags()).hasSize(1);
        assertThat(dto.tags().get(0).name()).isEqualTo("Thriller");
        assertThat(dto.tags().get(0).id()).isEqualTo(1L);
    }

    @Test
    void toItemDTO_emptyTags_returnsEmptyList() {
        Item item = new Item();
        item.setTags(new HashSet<>());

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.tags()).isEmpty();
    }

    @Test
    void toItemDTO_nullGenres_returnsEmptyList() {
        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setGenres(null);

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.genres()).isEmpty();
    }

    @Test
    void toItemDTO_mapsGenres() {
        Genre genre = new Genre();
        genre.setId(2L);

        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setGenres(Set.of(genre));

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.genres()).containsExactly(2L);
    }

    @Test
    void toItemDTO_nullSubgenres_returnsEmptyList() {
        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setSubgenres(null);

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.subgenres()).isEmpty();
    }

    @Test
    void toItemDTO_mapsSubgenres() {
        Subgenre subgenre = new Subgenre();
        subgenre.setId(3L);

        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setSubgenres(Set.of(subgenre));

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.subgenres()).containsExactly(3L);
    }

    @Test
    void toItemDTO_nullReadingLevel_returnsNull() {
        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setReadingLevel(null);

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.readingLevel()).isNull();
    }

    @Test
    void toItemDTO_mapsReadingLevel() {
        ReadingLevel level = new ReadingLevel();
        level.setId(5L);

        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setReadingLevel(level);

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.readingLevel()).isEqualTo(5L);
    }

    @Test
    void toItemDTO_nullDidacticCategories_returnsEmptyList() {
        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setDidacticCategories(null);

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.didacticCategories()).isEmpty();
    }

    @Test
    void toItemDTO_mapsDidacticCategories() {
        DidacticCategory cat = new DidacticCategory();
        cat.setId(7L);

        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setDidacticCategories(Set.of(cat));

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.didacticCategories()).containsExactly(7L);
    }

    @Test
    void toItemDTO_mapsIsDidactic_true() {
        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setDidactic(true);

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.isDidactic()).isTrue();
    }

    @Test
    void toItemDTO_mapsIsDidactic_false() {
        Item item = new Item();
        item.setTags(new HashSet<>());
        item.setDidactic(false);

        ItemDTO dto = mapper.toItemDTO(item);

        assertThat(dto.isDidactic()).isFalse();
    }
}