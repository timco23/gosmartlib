package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.*;
import edu.ap.spring_backend.jpa.*;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.exception.EntityAlreadyExistsException;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.ap.spring_backend.specification.ItemSpecifications.*;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final TagRepository tagRepository;
    private final SchoolItemCopyRepository schoolItemCopyRepository;
    public ItemService(ItemRepository itemRepository,  SchoolRepository schoolRepository, CampusRepository campusRepository, TagRepository tagRepository,  SchoolItemCopyRepository schoolItemCopyRepository) {
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
        this.tagRepository = tagRepository;
        this.schoolItemCopyRepository = schoolItemCopyRepository;
    }

    // Gets Items in page format
    public PageResponseDTO<ItemDTO> getFilteredItems(ItemRequestDTO request) {
        validateRequest(request);

        Specification<Item> spec = Specification
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

        Page<Item> items = itemRepository.findAll(spec, pageable);

        return new PageResponseDTO<>(items.map(this::mapToItemDTO));
    }

    // Stop illogical requests
    private void validateRequest(ItemRequestDTO request) {
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
            case "author" -> Sort.by(direction, "author");
            case "year" -> Sort.by(direction, "releaseYear");
            case "score" -> Sort.by(direction, "reviewScore");
            case "pages" -> Sort.by(direction, "pageCount");
            default -> Sort.by(direction, "title");
        };
    }

    // Adds an Item to the database.
    // If it doesn't already exist adds it, makes a SchoolItemCopy of it and links it to the school.
    // If it does already exist, makes a SchoolItemCopy that's linked to it and the school.
    public ItemDTO addItem(CreateItemDTO itemDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException {
        if (!schoolItemCopyRepository.existsByItemTitleAndSchoolId(itemDTO.title(), itemDTO.schoolId())) {
            Item newItem;
            if (!itemRepository.existsByTitle(itemDTO.title())) {
                Set<Tag> tags = getTagsById(itemDTO.tags());
                newItem = new Item(itemDTO.description(), itemDTO.reviewCount(), itemDTO.reviewScore(), itemDTO.cover(), itemDTO.pageCount(), itemDTO.bookLanguage(), itemDTO.releaseYear(), itemDTO.isbn(), itemDTO.author(), itemDTO.publisher(), itemDTO.title(), tags);
                itemRepository.save(newItem);
            }
            else {
                // itemId will be 0 if none has been given
                if (itemDTO.itemId() == 0) {
                    newItem = itemRepository.findByTitle(itemDTO.title()).orElseThrow(EntityNotFoundException::new);
                } else {
                    newItem = itemRepository.findById(itemDTO.itemId()).orElseThrow(EntityNotFoundException::new);
                }
            }
            School school = schoolRepository.findById(itemDTO.schoolId()).orElseThrow(EntityNotFoundException::new);
            Campus campus = null;
            if (itemDTO.campusId() != null) {
                campus = campusRepository.findById(itemDTO.campusId()).orElse(null);
            }
            SchoolItemCopy copy = new SchoolItemCopy(itemDTO.barcode(), itemDTO.featured(), newItem, school, campus, itemDTO.copyStatus(), itemDTO.condition());
            schoolItemCopyRepository.save(copy);
            return mapToItemDTO(newItem);
        }
        throw new EntityAlreadyExistsException();
    }

    //Deletes an Item from the database.
    public ItemDTO deleteItem(int id) throws EntityNotFoundException {
        Item schoolItem = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        itemRepository.delete(schoolItem);
        return mapToItemDTO(schoolItem);
    }

    // Gets all Items
    public List<ItemDTO> getAllItems() {
        List<Item> items = itemRepository.findAll();
        List<ItemDTO> itemDTOS = items.stream().map(item -> mapToItemDTO(item)).toList();
        return itemDTOS;
    }

    // Updates the details of an Item.
    public ItemDTO updateItem(int id, UpdateItemDTO itemDTO) throws EntityNotFoundException {
        Item currentItem = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        Set<Tag> tags = getTagsById(itemDTO.tags());
        Item newItem = new Item(itemDTO.description(), currentItem.getReviewCount(), currentItem.getReviewScore(), itemDTO.cover(), itemDTO.pageCount(), itemDTO.bookLanguage(), itemDTO.releaseYear(), itemDTO.isbn(), itemDTO.author(), itemDTO.publisher(), itemDTO.title(), tags);
        newItem.setId(currentItem.getId());
        itemRepository.save(newItem);
        return mapToItemDTO(newItem);
    }

    // Gets an Item by its id
    public ItemDTO getItemById(int id) throws EntityNotFoundException {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToItemDTO(item);
    }

    // Gets an Item by its title
    public ItemDTO getItemByTitle(String title) {
        Item item = itemRepository.findByTitle(title).orElse(null);
        if (item != null) {
            return mapToItemDTO(item);
        }
        return null;
    }

    // Gets a List of featured Items
    public List<ItemDTO> getFeaturedItems() {
        List<Item> featuredBooks = itemRepository.findAll(Specification.allOf(isFeatured(true)));
        return featuredBooks.stream().map(this::mapToItemDTO).toList();
    }

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

    // Converts an Item to an ItemDTO
    private ItemDTO mapToItemDTO(Item item) {
        List<TagDTO> tags = item.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .toList();

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
                item.getReviewCount(),
                item.getDescription(),
                tags);
    }
}
