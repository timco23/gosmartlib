package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemRepository;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.dao.TagRepository;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolItem;
import edu.ap.spring_backend.exception.EntityAlreadyExistsException;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.jpa.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import edu.ap.spring_backend.dao.CampusRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.ap.spring_backend.specification.SchoolItemSpecifications.*;

@Service
public class ItemService {
    private final SchoolItemRepository schoolItemRepository;
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final TagRepository tagRepository;
    public ItemService(SchoolItemRepository schoolItemRepository, ItemRepository itemRepository,  SchoolRepository schoolRepository, CampusRepository campusRepository, TagRepository tagRepository) {
        this.schoolItemRepository = schoolItemRepository;
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
        this.tagRepository = tagRepository;
    }

    // Gets SchoolItems in page format
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

    // Stop illogical requests
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

    // Adds a SchoolItem. Checks first if school already has this SchoolItem, if not
    // then if the Item isn't already in the database, adds it there first before
    // linking it to a school.
    public SchoolItemDTO addSchoolItem(CreateSchoolItemDTO itemDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException {
        if (!schoolItemRepository.existsByItemTitle(itemDTO.title())) {
            Item newItem;

            if (!itemRepository.existsByTitle(itemDTO.title())) {
                newItem = new Item(itemDTO.title(), itemDTO.publisher(), itemDTO.author(), itemDTO.isbn(),
                        itemDTO.releaseYear(), itemDTO.bookLanguage(), itemDTO.pageCount(), itemDTO.cover(),
                        itemDTO.reviewScore(), itemDTO.reviewCount());
                itemRepository.save(newItem);
            }
            // itemId will be 0 if none has been given
            else if (itemDTO.itemId() == 0) {
                newItem = itemRepository.findByTitle(itemDTO.title()).orElseThrow(EntityNotFoundException::new);
            } else {
                newItem = itemRepository.findById(itemDTO.itemId()).orElseThrow(EntityNotFoundException::new);
            }
            School school = schoolRepository.findById((long) itemDTO.schoolId()).orElseThrow(EntityNotFoundException::new);
            Campus campus = campusRepository.findById((long) itemDTO.campusId()).orElse(null);
            Set<Tag> tags = getTagsById(itemDTO.tags());
            SchoolItem newSchoolItem = new SchoolItem(school, tags, itemDTO.description(), itemDTO.featured(), newItem, campus);

            schoolItemRepository.save(newSchoolItem);
            return mapToSchoolItemDTO(newSchoolItem);
        }
        throw new EntityAlreadyExistsException();
    }

    // Checks if given id has a SchoolItem that's linked to the school attached. If
    // so deletes it
    public SchoolItemDTO deleteSchoolItem(int id) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        schoolItemRepository.delete(schoolItem);
        return mapToSchoolItemDTO(schoolItem);
    }

    // Gets all Items
    public List<ItemDTO> getAllItems() {
        List<Item> items = itemRepository.findAll();
        List<ItemDTO> itemDTOS = items.stream().map(item -> mapToItemDTO(item)).toList();
        return itemDTOS;
    }

    // Updates the details of a SchoolItem alongside the Item it's linked to.
    public SchoolItemDTO updateSchoolItem(int id, UpdateSchoolItemDTO schoolItemDTO) throws EntityNotFoundException {
        SchoolItem currentSchoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        Item newItem = new Item(schoolItemDTO.title(), schoolItemDTO.publisher(), schoolItemDTO.author(),
                currentSchoolItem.getItem().getIsbn(), schoolItemDTO.releaseYear(), schoolItemDTO.bookLanguage(),
                schoolItemDTO.pageCount(), schoolItemDTO.cover(), currentSchoolItem.getItem().getReviewScore(),
                currentSchoolItem.getItem().getReviewCount());
        newItem.setId(currentSchoolItem.getItem().getId());
        Campus newCampus = campusRepository.findById((long) schoolItemDTO.campusId()).orElse(null);
        Set<Tag> tags = getTagsById(schoolItemDTO.tags());
        SchoolItem newSchoolItem = new SchoolItem(currentSchoolItem.getSchool(), tags, schoolItemDTO.description(), schoolItemDTO.featured(), newItem, newCampus);
        newSchoolItem.setId(id);

        itemRepository.save(newItem);
        schoolItemRepository.save(newSchoolItem);
        return mapToSchoolItemDTO(newSchoolItem);
    }

    // Gets a SchoolItem by its id
    public SchoolItemDTO getSchoolItemById(int id) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToSchoolItemDTO(schoolItem);
    }

    // Gets an Item by its id
    public ItemDTO getItemById(int id) throws EntityNotFoundException {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToItemDTO(item);
    }

    // Gets a SchoolItem by its title
    public SchoolItemDTO getSchoolItemByTitle(String title) {
        SchoolItem schoolItem = schoolItemRepository.findByItemTitle(title).orElse(null);
        if (schoolItem != null) {
            return mapToSchoolItemDTO(schoolItem);
        }
        return null;
    }

    // Gets a List of featured SchoolItems
    public List<SchoolItemDTO> getFeaturedSchoolItems() {
        List<SchoolItem> featuredBooks = schoolItemRepository.findAll(Specification.allOf(isFeatured(true)));
        return featuredBooks.stream().map(this::mapToSchoolItemDTO).toList();
    }

    //Converts a SchoolItem to a SchoolItemDTO
    // Takes a list of tag IDs, looks each one up in the database and returns them
    // as a Set<Tag>
    // because a book cannot have the same tag twice
    private Set<Tag> getTagsById(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return new HashSet<>();

        Set<Tag> foundTags = new HashSet<>();
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(EntityNotFoundException::new);
            foundTags.add(tag);
        }
        return foundTags;
    }

    // Converts a SchoolItem to an SchoolItemDTO
    private SchoolItemDTO mapToSchoolItemDTO(SchoolItem item) {
        List<TagDTO> tags = item.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
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
                item.getDescription(),
                item.isFeatured(),
                item.getCampus() != null ? item.getCampus().getName() : null,
                item.getCampus() != null ? item.getCampus().getId().intValue() : 0);
    }

    // Converts an Item to an ItemDTO
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
