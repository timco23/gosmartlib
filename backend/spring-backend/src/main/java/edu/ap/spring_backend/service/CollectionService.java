package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.*;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.exception.EmptyCollectionException;
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

    public List<CollectionDTO> findBySchoolId(Long schoolId) {
        List<Collection> collections;
        if (schoolId == 0) {
            collections = collectionRepository.findBySchoolId(null);
        } else {
            collections = collectionRepository.findBySchoolId(schoolId);
        }
        return collections.stream().map(this::mapToCollectionDTO).toList();
    }

    public CollectionDTO findById(Long id) {
        Collection collection = collectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToCollectionDTO(collection);
    }

    public List<CollectionDTO> getFeaturedBySchoolId(Long schoolId) {
        return collectionRepository.findBySchoolIdAndFeatured(schoolId, true)
                .stream().map(this::mapToCollectionDTO).toList();
    }
    
    public List<CollectionDTO> getByGloballyFeatured() {
        return collectionRepository.findByGloballyFeatured(true)
                .stream().map(this::mapToCollectionDTO).toList();
    }

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

        throw new EmptyCollectionException();
    }

    public CollectionDTO updateCollection(CreateCollectionDTO collectionDTO, Long id) {
        Collection collection = collectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        List<Item> newItems = new ArrayList<>();
        for (Long itemId : collectionDTO.itemIds()) {
            Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
            newItems.add(item);
        }

        if (newItems.isEmpty()) {
            throw new EmptyCollectionException();
        }

        collection.setTitle(collectionDTO.title());
        collection.setDescription(collectionDTO.description());
        collection.setFeatured(collectionDTO.featured());
        collection.setGloballyFeatured(collectionDTO.globallyFeatured());

        collection.getItems().clear();
        collection.getItems().addAll(newItems);

        collectionRepository.save(collection);
        return mapToCollectionDTO(collection);
    }

    public CollectionDTO deleteCollection(Long id) {
        Collection collection = collectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        collectionRepository.delete(collection);
        return mapToCollectionDTO(collection);
    }

    public boolean isItemInAnyCollection(Long itemId) {
        return collectionRepository.existsByItems_Id(itemId);
    }

    public boolean isItemInAnyGloballyFeaturedCollection(Long itemId) {
        return collectionRepository.existsByItems_IdAndGloballyFeatured(itemId, true);
    }

    /**
     * Converts a Collection entity to a CollectionDTO, including mapping its associated Items to ItemDTOs. This method is used to transform the data from the database format to the format expected by the API clients. It handles the mapping of all relevant fields and relationships, ensuring that the resulting CollectionDTO contains all necessary information for the frontend to display collections properly.
      * @param collection the Collection entity to convert
      * @return a CollectionDTO containing the data from the Collection entity, including mapped ItemDTOs for each associated Item
     */
    private CollectionDTO mapToCollectionDTO(Collection collection) {
        return new CollectionDTO(
                collection.getId(),
                collection.getTitle(),
                collection.getDescription(),
                collection.getFeatured(),
                collection.getGloballyFeatured(),
                collection.getItems().stream().map(this::mapToItemDTO).toList(),
                collection.getSchool() != null ? collection.getSchool().getId() : null
        );
    }

    /**
     * Converts an Item entity to an ItemDTO, including mapping its associated Tags, Genres, Subgenres, ReadingLevel, and DidacticCategories to their respective DTOs. This method is used as part of the mapping process when converting a Collection entity to a CollectionDTO, ensuring that all relevant data about the Items in the collection is included in the API response. It handles potential null values and ensures that the resulting ItemDTO contains all necessary information for the frontend to display item details properly.
     * @param item
     * @return an ItemDTO containing the data from the Item entity, including mapped TagDTOs for each associated Tag, genre and subgenre IDs, reading level ID, and didactic category IDs.
     */
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
                null,
                false,
                item.isDidactic(),
                null
        );
    }
}
