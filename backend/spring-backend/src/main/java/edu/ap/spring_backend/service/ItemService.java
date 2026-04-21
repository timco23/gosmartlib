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
import java.util.stream.Collectors;
import static edu.ap.spring_backend.specification.ItemSpecifications.*;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final TagRepository tagRepository;
    private final GenreRepository genreRepository;
    private final SubgenreRepository subgenreRepository;
    private final ReadingLevelRepository readingLevelRepository;
    private final DidacticCategoryRepository didacticCategoryRepository;
    private final SchoolItemCopyRepository schoolItemCopyRepository;
    

    public ItemService(ItemRepository itemRepository, SchoolRepository schoolRepository,
            CampusRepository campusRepository, TagRepository tagRepository,
            SchoolItemCopyRepository schoolItemCopyRepository,
            GenreRepository genreRepository, SubgenreRepository subgenreRepository,
            ReadingLevelRepository readingLevelRepository, DidacticCategoryRepository didacticCategoryRepository) {
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
        this.tagRepository = tagRepository;
        this.genreRepository = genreRepository;
        this.subgenreRepository = subgenreRepository;
        this.readingLevelRepository = readingLevelRepository;
        this.didacticCategoryRepository = didacticCategoryRepository;
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
        .and(withMinScore(request.minScore()))
        .and(withTags(request.tagIds()))
        .and(withGenres(request.genreIds()))
        .and(withSubgenres(request.subgenreIds()))
        .and(withDidactic(request.didactic()))
        .and(withDidacticCategories(Boolean.TRUE.equals(request.didactic()) ? request.didacticCategoryIds() : null));

        if(!request.globalBooks()) {
            spec = spec.and(withSchoolId(request.schoolId()));
        }
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
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return switch (sortBy) {
            case "author" -> Sort.by(direction, "author");
            case "score" -> Sort.by(direction, "reviewScore");
            case "pages" -> Sort.by(direction, "pageCount");
            default -> Sort.by(direction, "title");
        };
    }

    // Adds an Item to the database.
    // If it doesn't already exist adds it, makes a SchoolItemCopy of it and links
    // it to the school.
    // If it does already exist, makes a SchoolItemCopy that's linked to it and the
    // school.
    public ItemDTO addItem(CreateItemDTO itemDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException {
        if (!schoolItemCopyRepository.existsByItemTitleAndSchoolId(itemDTO.title(), itemDTO.schoolId())) {
            Item newItem;
            if (!itemRepository.existsByTitle(itemDTO.title())) {
                Set<Tag> tags = getTagsById(itemDTO.tags());
                newItem = new Item(itemDTO.description(), itemDTO.reviewCount(), itemDTO.reviewScore(), itemDTO.cover(),
                        itemDTO.pageCount(), itemDTO.bookLanguage(), itemDTO.releaseYear(), itemDTO.isbn(),
                        itemDTO.author(), itemDTO.publisher(), itemDTO.title(), tags);
                itemRepository.save(newItem);
            } else {
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
            SchoolItemCopy copy = new SchoolItemCopy(null, itemDTO.featured(), newItem, school, campus,
                    itemDTO.copyStatus(), itemDTO.condition());
            Set<Tag> tags = getTagsById(itemDTO.tags());
            Set<Genre> genres = getGenresById(itemDTO.genres());
            Set<Subgenre> subgenres = getSubgenresById(itemDTO.subgenres());
            ReadingLevel readingLevel = getReadingLevelById(itemDTO.readingLevel());
            Set<DidacticCategory> didacticCategories = getDidacticCategoriesById(itemDTO.didacticCategories());

            newItem.setTags(tags);
            newItem.setGenres(genres);
            newItem.setSubgenres(subgenres);
            newItem.setReadingLevel(readingLevel);
            newItem.setDidacticCategories(didacticCategories);
            newItem.setDidactic(itemDTO.isDidactic() || !didacticCategories.isEmpty());

            SchoolItemCopy savedCopy = schoolItemCopyRepository.save(copy);
            savedCopy.setBarcode(String.format("BC%06d", savedCopy.getId()));
            schoolItemCopyRepository.save(savedCopy);
            return mapToItemDTO(newItem);
        }
        throw new EntityAlreadyExistsException();
    }

    // Deletes an Item from the database.
    public ItemDTO deleteItem(Long id) throws EntityNotFoundException {
        Item schoolItem = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        itemRepository.delete(schoolItem);
        return mapToItemDTO(schoolItem);
    }

    public List<ItemDTO> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map(this::mapToItemDTO).toList();
    }

    // Updates the details of an Item.
    public ItemDTO updateItem(Long id, UpdateItemDTO itemDTO) throws EntityNotFoundException {
    Item item = itemRepository.findById(id)
            .orElseThrow(EntityNotFoundException::new);

        item.setTitle(itemDTO.title());
        item.setAuthor(itemDTO.author());
        item.setPublisher(itemDTO.publisher());
        item.setIsbn(itemDTO.isbn());
        item.setReleaseYear(itemDTO.releaseYear());
        item.setBookLanguage(itemDTO.bookLanguage());
        item.setPageCount(itemDTO.pageCount());
        item.setCover(itemDTO.cover());
        item.setDescription(itemDTO.description());
        
        item.setTags(getTagsById(itemDTO.tags()));
        item.setGenres(getGenresById(itemDTO.genres()));
        item.setSubgenres(getSubgenresById(itemDTO.subgenres()));
        item.setReadingLevel(getReadingLevelById(itemDTO.readingLevel()));
        Set<DidacticCategory> updatedDidacticCategories = getDidacticCategoriesById(itemDTO.didacticCategories());
        item.setDidacticCategories(updatedDidacticCategories);
        item.setDidactic(itemDTO.isDidactic() || !updatedDidacticCategories.isEmpty());

    itemRepository.save(item);

    return mapToItemDTO(item);
}

    public ItemDTO getItemById(Long id) throws EntityNotFoundException {
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
    public List<ItemDTO> getFeaturedItems(Long schoolId) {
        List<Item> featuredBooks = itemRepository.findAll(Specification.allOf(isFeatured(true, schoolId)));
        return featuredBooks.stream().map(this::mapToItemDTO).toList();
    }

    private Set<Tag> getTagsById(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return new HashSet<>();
        return tagIds.stream()
                .map(id -> tagRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    private Set<Genre> getGenresById(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty())
            return new HashSet<>();
        return genreIds.stream()
                .map(id -> genreRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    private Set<Subgenre> getSubgenresById(List<Long> subgenreIds) {
        if (subgenreIds == null || subgenreIds.isEmpty())
            return new HashSet<>();
        return subgenreIds.stream()
                .map(id -> subgenreRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    // FIX: Long -> intValue() omdat ReadingLevel entity int id gebruikt
    private ReadingLevel getReadingLevelById(Long id) {
        if (id == null)
            return null;
        return readingLevelRepository.findById(id.intValue()).orElse(null);
    }

    // Converts an Item to an ItemDTO
    private ItemDTO mapToItemDTO(Item item) {
        List<TagDTO> tags = item.getTags().stream()
                .map(tag -> new TagDTO((long) tag.getId(), tag.getName()))
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
                tags,
                item.getGenres() != null ? item.getGenres().stream().map(g -> (long) g.getId()).toList() : List.of(),
                item.getSubgenres() != null ? item.getSubgenres().stream().map(s -> (long) s.getId()).toList()
                        : List.of(),
                item.getReadingLevel() != null ? (long) item.getReadingLevel().getId() : null,
                item.getDidacticCategories() != null
                        ? item.getDidacticCategories().stream().map(d -> (long) d.getId()).toList()
                        : List.of(),
                null, // schoolId (of juiste waarde)
                false, // featured (of uit copy halen)
                item.isDidactic(),
                null // campusId
        );
    }

    private Set<DidacticCategory> getDidacticCategoriesById(List<Long> didacticCategoryIds) {
        if (didacticCategoryIds == null || didacticCategoryIds.isEmpty())
            return new HashSet<>();
        return didacticCategoryIds.stream()
                .map(id -> didacticCategoryRepository.findById(id).orElseThrow(EntityNotFoundException::new))
                .collect(Collectors.toSet());
    }

    public List<ItemDTO> get10LatestItems(Long schoolId) {
        List<SchoolItemCopy> copies = schoolItemCopyRepository.findTop50BySchoolIdOrderByIdDesc(schoolId);

        Set<Long> seenIds = new HashSet<>();
        return copies.stream()
                .map(SchoolItemCopy::getItem)
                .filter(item -> seenIds.add(item.getId()))
                .limit(10)
                .map(this::mapToItemDTO)
                .toList();
    }
}