package edu.ap.spring_backend.service;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.ReadingLevelRepository;
import edu.ap.spring_backend.dao.SchoolItemRepository;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.DidacticCategory;
import edu.ap.spring_backend.jpa.Genre;
import edu.ap.spring_backend.dao.TagRepository;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.ReadingLevel;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolItem;
import edu.ap.spring_backend.jpa.Subgenre;
import edu.ap.spring_backend.exception.EntityAlreadyExistsException;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.SubgenreRepository;
import edu.ap.spring_backend.jpa.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import edu.ap.spring_backend.dao.CampusRepository;
import edu.ap.spring_backend.dao.DidacticCategoryRepository;
import edu.ap.spring_backend.dao.GenreRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static edu.ap.spring_backend.specification.SchoolItemSpecifications.*;

@Service
public class ItemService {
    private final SchoolItemRepository schoolItemRepository;
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final TagRepository tagRepository;
    private final GenreRepository genreRepository;
    private final SubgenreRepository subgenreRepository;
    private final ReadingLevelRepository readingLevelRepository;
    private final DidacticCategoryRepository didacticCategoryRepository;

    public ItemService(SchoolItemRepository schoolItemRepository, ItemRepository itemRepository,
            SchoolRepository schoolRepository, CampusRepository campusRepository, TagRepository tagRepository,
            GenreRepository genreRepository, SubgenreRepository subgenreRepository,
            ReadingLevelRepository readingLevelRepository, DidacticCategoryRepository didacticCategoryRepository) {
        this.schoolItemRepository = schoolItemRepository;
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
        this.tagRepository = tagRepository;
        this.genreRepository = genreRepository;
        this.subgenreRepository = subgenreRepository;
        this.readingLevelRepository = readingLevelRepository;
        this.didacticCategoryRepository = didacticCategoryRepository;
    }

    public PageResponseDTO<SchoolItemDTO> getSchoolItems(SchoolItemRequestDTO request) {
        validateRequest(request);
        Specification<SchoolItem> spec = Specification
                .allOf(withSearch(request.search()))
                .and(withLanguages(request.languages()))
                .and(withMinPages(request.minPages()))
                .and(withMaxPages(request.maxPages()))
                .and(withMinYear(request.minYear()))
                .and(withMaxYear(request.maxYear()))
                .and(withMinScore(request.minScore()))
                .and(withTags(request.tagIds()));
        Sort sort = createSort(request.sortBy(), request.sortDirection());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);
        Page<SchoolItem> schoolItems = schoolItemRepository.findAll(spec, pageable);
        return new PageResponseDTO<>(schoolItems.map(this::mapToSchoolItemDTO));
    }

    private void validateRequest(SchoolItemRequestDTO request) {
        if (request.minPages() != null && request.maxPages() != null
                && request.minPages() > request.maxPages()) {
            throw new IllegalArgumentException("minPages cannot be greater than maxPages");
        }
        if (request.minYear() != null && request.maxYear() != null
                && request.minYear() > request.maxYear()) {
            throw new IllegalArgumentException("minYear cannot be greater maxYear");
        }
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return switch (sortBy) {
            case "author" -> Sort.by(direction, "item.author");
            case "year" -> Sort.by(direction, "item.releaseYear");
            case "score" -> Sort.by(direction, "item.reviewScore");
            case "pages" -> Sort.by(direction, "item.pageCount");
            default -> Sort.by(direction, "item.title");
        };
    }

    public SchoolItemDTO addSchoolItem(CreateSchoolItemDTO itemDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException {
        if (!schoolItemRepository.existsByItemTitle(itemDTO.title())) {
            Item newItem;
            if (!itemRepository.existsByTitle(itemDTO.title())) {
                newItem = new Item(itemDTO.title(), itemDTO.publisher(), itemDTO.author(), itemDTO.isbn(),
                        itemDTO.releaseYear(), itemDTO.bookLanguage(), itemDTO.pageCount(), itemDTO.cover(),
                        itemDTO.reviewScore(), itemDTO.reviewCount());
                itemRepository.save(newItem);
            } else if (itemDTO.itemId() == 0) {
                newItem = itemRepository.findByTitle(itemDTO.title()).orElseThrow(EntityNotFoundException::new);
            } else {
                newItem = itemRepository.findById(itemDTO.itemId()).orElseThrow(EntityNotFoundException::new);
            }

            School school = schoolRepository.findById((long) itemDTO.schoolId()).orElseThrow(EntityNotFoundException::new);
            Campus campus = campusRepository.findById((long) itemDTO.campusId()).orElse(null);

            Set<Tag> tags = getTagsById(itemDTO.tags());
            Set<Genre> genres = getGenresById(itemDTO.genres());
            Set<Subgenre> subgenres = getSubgenresById(itemDTO.subgenres());
            ReadingLevel readingLevel = getReadingLevelById(itemDTO.readingLevel());
            Set<DidacticCategory> didacticCategories = getDidacticCategoriesById(itemDTO.didacticCategories());

            SchoolItem newSchoolItem = new SchoolItem(school, tags, genres, subgenres, readingLevel,
                    didacticCategories, itemDTO.description(), itemDTO.featured(), newItem, campus, itemDTO.isDidactic());
            schoolItemRepository.save(newSchoolItem);
            return mapToSchoolItemDTO(newSchoolItem);
        }
        throw new EntityAlreadyExistsException();
    }

    public SchoolItemDTO deleteSchoolItem(int id) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        schoolItemRepository.delete(schoolItem);
        return mapToSchoolItemDTO(schoolItem);
    }

    public List<ItemDTO> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map(this::mapToItemDTO).toList();
    }

    public SchoolItemDTO updateSchoolItem(int id, UpdateSchoolItemDTO schoolItemDTO) throws EntityNotFoundException {
        SchoolItem currentSchoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        Item newItem = new Item(schoolItemDTO.title(), schoolItemDTO.publisher(), schoolItemDTO.author(),
                currentSchoolItem.getItem().getIsbn(), schoolItemDTO.releaseYear(), schoolItemDTO.bookLanguage(),
                schoolItemDTO.pageCount(), schoolItemDTO.cover(), currentSchoolItem.getItem().getReviewScore(),
                currentSchoolItem.getItem().getReviewCount());
        newItem.setId(currentSchoolItem.getItem().getId());

        Campus newCampus = campusRepository.findById((long) schoolItemDTO.campusId()).orElse(null);

        Set<Tag> tags = getTagsById(schoolItemDTO.tags());
        Set<Genre> genres = getGenresById(schoolItemDTO.genres());
        Set<Subgenre> subgenres = getSubgenresById(schoolItemDTO.subgenres());
        ReadingLevel readingLevel = getReadingLevelById(schoolItemDTO.readingLevel());
        Set<DidacticCategory> didacticCategories = getDidacticCategoriesById(schoolItemDTO.didacticCategories());

        SchoolItem newSchoolItem = new SchoolItem(currentSchoolItem.getSchool(), tags, genres, subgenres,
                readingLevel, didacticCategories, schoolItemDTO.description(), schoolItemDTO.featured(), newItem, newCampus, schoolItemDTO.isDidactic());
        newSchoolItem.setId(id);

        itemRepository.save(newItem);
        schoolItemRepository.save(newSchoolItem);
        return mapToSchoolItemDTO(newSchoolItem);
    }

    public SchoolItemDTO getSchoolItemById(int id) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToSchoolItemDTO(schoolItem);
    }

    public ItemDTO getItemById(int id) throws EntityNotFoundException {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToItemDTO(item);
    }

    public SchoolItemDTO getSchoolItemByTitle(String title) {
        SchoolItem schoolItem = schoolItemRepository.findByItemTitle(title).orElse(null);
        if (schoolItem != null) {
            return mapToSchoolItemDTO(schoolItem);
        }
        return null;
    }

    public List<SchoolItemDTO> getFeaturedSchoolItems() {
        List<SchoolItem> featuredBooks = schoolItemRepository.findAll(Specification.allOf(isFeatured(true)));
        return featuredBooks.stream().map(this::mapToSchoolItemDTO).toList();
    }

    private Set<Tag> getTagsById(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return new HashSet<>();
        return tagIds.stream()
                .map(id -> tagRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    private Set<Genre> getGenresById(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return new HashSet<>();
        return genreIds.stream()
                .map(id -> genreRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    private Set<Subgenre> getSubgenresById(List<Long> subgenreIds) {
        if (subgenreIds == null || subgenreIds.isEmpty()) return new HashSet<>();
        return subgenreIds.stream()
                .map(id -> subgenreRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    // FIX: Long -> intValue() omdat ReadingLevel entity int id gebruikt
    private ReadingLevel getReadingLevelById(Long id) {
        if (id == null) return null;
        return readingLevelRepository.findById(id.intValue()).orElse(null);
    }

    private Set<DidacticCategory> getDidacticCategoriesById(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return new HashSet<>();
        return categoryIds.stream()
                .map(id -> didacticCategoryRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    private SchoolItemDTO mapToSchoolItemDTO(SchoolItem item) {
        List<TagDTO> tags = item.getTags().stream()
                .map(tag -> new TagDTO((long) tag.getId(), tag.getName()))
                .toList();
        List<GenreDTO> genres = item.getGenres().stream()
                .map(genre -> new GenreDTO((long) genre.getId(), genre.getName()))
                .toList();
        List<SubgenreDTO> subgenres = item.getSubgenres().stream()
                .map(subgenre -> new SubgenreDTO((int) subgenre.getId(), subgenre.getName()))
                .toList();
        List<DidacticCategoryDTO> didacticCategories = item.getDidacticCategories().stream()
                .map(dc -> new DidacticCategoryDTO((long) dc.getId(), dc.getName()))
                .toList();

        return new SchoolItemDTO(
        item.getId(),
        item.getItem().getTitle(),
        item.getItem().getPublisher(),
        item.getItem().getAuthor(),
        item.getItem().getIsbn(),
        item.getItem().getReleaseYear(),
        item.getItem().getBookLanguage(),
        item.getItem().getPageCount(),
        item.getItem().getCover(),
        item.getItem().getReviewScore(),
        item.getItem().getReviewCount(),
        item.getItem().getId(),
        item.getSchool().getId().intValue(),
        tags,
        genres,
        subgenres,
        item.getReadingLevel(),
        didacticCategories,
        item.getDescription(),
        item.isFeatured(),
        item.getCampus() != null ? item.getCampus().getName() : null,
        item.getCampus() != null ? item.getCampus().getId().intValue() : 0,
        item.isDidactic());
    }

    private ItemDTO mapToItemDTO(Item item) {
        return new ItemDTO(
                item.getId(),
                item.getTitle(),
                item.getPublisher(),
                item.getAuthor(),
                item.getIsbn(),
                item.getReleaseYear(),
                item.getBookLanguage(),
                item.getPageCount(),
                item.getCover(),
                item.getReviewScore(),
                item.getReviewCount());
    }
}