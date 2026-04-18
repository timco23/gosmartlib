package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.*;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Collection;
import edu.ap.spring_backend.jpa.Item;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;

    public CollectionService(CollectionRepository collectionRepository, ItemRepository itemRepository,  SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
        this.itemRepository = itemRepository;
        this.collectionRepository = collectionRepository;
    }

    //Gets all collections by their school id
    public List<CollectionDTO> findBySchoolId(Long schoolId) {
        List<Collection> collections;
        if (schoolId == 0) {
            collections = collectionRepository.findBySchoolId(null);
        }
        else {
            collections = collectionRepository.findBySchoolId(schoolId);
        }
        return collections.stream().map(collection -> mapToCollectionDTO(collection)).toList();
    }

    //Gets a collection by its id
    public CollectionDTO findById(Long id) {
        Collection collection = collectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToCollectionDTO(collection);
    }

    //Gets all featured collections that a school has
    public List<CollectionDTO> getFeaturedBySchoolId(Long schoolId) {
        List<Collection> collections = collectionRepository.findBySchoolIdAndFeatured(schoolId, true);
        return collections.stream().map(collection -> mapToCollectionDTO(collection)).toList();
    }

    //Gets all globally featured collections
    public List<CollectionDTO> getByGloballyFeatured() {
        List<Collection> collections = collectionRepository.findByGloballyFeatured(true);
        return collections.stream().map(collection -> mapToCollectionDTO(collection)).toList();
    }

    //Adds a collection to the database
    public CollectionDTO addCollection(CreateCollectionDTO collectionDTO) {
        List<Item> items = new ArrayList<>();
        collectionDTO.itemIds().forEach(itemId -> {
            Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
            items.add(item);
        });

        if (!items.isEmpty()) {
            Collection collection = new Collection(
                    collectionDTO.title(),
                    collectionDTO.description(),
                    schoolRepository.findById(collectionDTO.schoolId()).orElse(null),
                    items,
                    collectionDTO.featured(),
                    collectionDTO.globallyFeatured()
            );
            collectionRepository.save(collection);
            return mapToCollectionDTO(collection);
        }

        throw new RuntimeException("Cannot add collection with no items.");
    }

    //Updates a collection in the database according to its id
    public CollectionDTO updateCollection(CreateCollectionDTO collectionDTO, Long id) {
        Collection collection = collectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        List<Item> newItems = new ArrayList<>();
        collectionDTO.itemIds().forEach(itemId -> {
            Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
            newItems.add(item);
        });
        if(!newItems.isEmpty()) {
            collection.setTitle(collectionDTO.title());
            collection.setDescription(collectionDTO.description());
            collection.setItems(newItems);
            collection.setFeatured(collectionDTO.featured());
            collection.setGloballyFeatured(collectionDTO.globallyFeatured());

            collectionRepository.save(collection);
            return mapToCollectionDTO(collection);
        }

        throw new RuntimeException("Cannot update collection with no items.");
    }

    //Deletes a collection in the database according to its id
    public CollectionDTO deleteCollection(Long id) {
        Collection collection = collectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        collectionRepository.delete(collection);
        return mapToCollectionDTO(collection);
    }

    //Checks if a given item is in any collection by its id
    public boolean isItemInAnyCollection(Long itemId) {
        return collectionRepository.existsByItems_Id(itemId);
    }

    //Checks if a given item is in any current globally featured collection by its id
    public boolean isItemInAnyGloballyFeaturedCollection(Long itemId) {
        return collectionRepository.existsByItems_IdAndGloballyFeatured(itemId, true);
    }

    //Converts a Collection to a CollectionDTO
    private CollectionDTO mapToCollectionDTO(Collection collection){
        return new CollectionDTO(
                collection.getId(),
                collection.getTitle(),
                collection.getDescription(),
                collection.getFeatured(),
                collection.getGloballyFeatured(),
                collection.getItems().stream().map(item -> mapToItemDTO(item)).toList(),
                collection.getSchool()
        );
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
}
