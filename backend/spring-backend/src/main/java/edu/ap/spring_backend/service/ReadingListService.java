package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.ReadingListRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.CreateReadingListDTO;
import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.ReadingListDTO;
import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.ReadingList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReadingListService {
    private final ReadingListRepository readingListRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ReadingListService(ReadingListRepository readingListRepository, ItemRepository itemRepository,  UserRepository userRepository) {
        this.readingListRepository = readingListRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    //Gets all reading lists
    public List<ReadingListDTO> findAll() {
        return readingListRepository.findAll().stream().map(list -> mapToReadingListDTO(list)).toList();
    }

    //Gets a reading list by its id
    public ReadingListDTO findById(Long id) {
        ReadingList readingList = readingListRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToReadingListDTO(readingList);
    }

    //Gets all reading lists that a user has by their id
    public List<ReadingListDTO> findByUserId(int userId) {
        List<ReadingList> list = readingListRepository.findByUserId(userId);
        return list.stream().map(readingList -> mapToReadingListDTO(readingList)).toList();
    }

    //Adds a reading list to the database
    public ReadingListDTO addReadingList(CreateReadingListDTO readingListDTO) {
        List<Item> items = new ArrayList<>();
        readingListDTO.itemIds().forEach(itemId -> {
            Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
            items.add(item);
        });

        if (!items.isEmpty()) {
            ReadingList readingList = new ReadingList(
                    readingListDTO.title(),
                    readingListDTO.description(),
                    userRepository.findById(readingListDTO.userId()).orElseThrow(EntityNotFoundException::new),
                    items
            );
            readingListRepository.save(readingList);
            return mapToReadingListDTO(readingList);
        }

        throw new RuntimeException("Cannot add reading list with no items.");
    }

    //Updates a reading list in the database according to its id
    public ReadingListDTO updateReadingList(CreateReadingListDTO readingListDTO, Long id) {
        ReadingList readingList = readingListRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        List<Item> newItems = new ArrayList<>();
        readingListDTO.itemIds().forEach(itemId -> {
            Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
            newItems.add(item);
        });
        if(!newItems.isEmpty()) {
            readingList.setTitle(readingListDTO.title());
            readingList.setDescription(readingListDTO.description());
            readingList.setItems(newItems);
            readingListRepository.save(readingList);
            return mapToReadingListDTO(readingList);
        }

        throw new RuntimeException("Cannot update reading list with no items.");
    }

    //Deletes a reading list in the database according to its id
    public ReadingListDTO deleteReadingList(Long id) {
        ReadingList readingList = readingListRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        readingListRepository.delete(readingList);
        return mapToReadingListDTO(readingList);
    }

    //Checks if a given item is in any reading list by its id
    public boolean isItemInAnyReadingList(Long itemId) {
        return readingListRepository.existsByItems_Id(itemId);
    }

    //Converts a ReadingList to a ReadingListDTO
    private ReadingListDTO mapToReadingListDTO(ReadingList readingList){
        return new ReadingListDTO(
                readingList.getId(),
                readingList.getTitle(),
                readingList.getDescription(),
                readingList.getUser().getFirstName(),
                readingList.getUser().getName(),
                readingList.getItems().stream().map(item -> mapToItemDTO(item)).toList()
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
