package edu.ap.spring_backend.service;

import edu.ap.spring_backend.DAO.ItemRepository;
import edu.ap.spring_backend.DAO.SchoolItemRepository;
import edu.ap.spring_backend.controllers.CreateSchoolItemDTO;
import edu.ap.spring_backend.controllers.ItemDTO;
import edu.ap.spring_backend.controllers.SchoolItemDTO;
import edu.ap.spring_backend.controllers.UpdateSchoolItemDTO;
import edu.ap.spring_backend.entity.Item;
import edu.ap.spring_backend.entity.SchoolItem;
import edu.ap.spring_backend.exceptions.EntityAlreadyExistsException;
import edu.ap.spring_backend.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private final SchoolItemRepository schoolItemRepository;
    private final ItemRepository itemRepository;
    public ItemService(SchoolItemRepository schoolItemRepository, ItemRepository itemRepository) {
        this.schoolItemRepository = schoolItemRepository;
        this.itemRepository = itemRepository;
    }

    // Gets all items that a school has
    public List<SchoolItemDTO> getAllSchoolItems(){
        List<SchoolItem> schoolItems = schoolItemRepository.findAll();
        List<SchoolItemDTO> schoolItemDTOS = schoolItems.stream().map(item -> mapToSchoolItemDTO(item)).toList();

        return schoolItemDTOS;
    }

    //Adds item under a school. Checks first if school already has this item, if not then if the item isn't already in the database, adds it there first before linking it to a school.
    public SchoolItemDTO addItem(CreateSchoolItemDTO itemDTO) throws EntityNotFoundException, EntityAlreadyExistsException {
        if (!schoolItemRepository.existsByItemTitle(itemDTO.title())) {
            Item newItem;

            if (!itemRepository.existsByTitle(itemDTO.title())) {
                newItem = new Item(itemDTO.title(), itemDTO.publisher(), itemDTO.author(), itemDTO.isbn(), itemDTO.releaseYear(), itemDTO.bookLanguage(), itemDTO.pageCount(), itemDTO.cover(), itemDTO.reviewScore(), itemDTO.reviewCount());
                itemRepository.save(newItem);
            }
            // itemId will be 0 if none has been given
            else if (itemDTO.itemId() == 0) {
                newItem = itemRepository.findByTitle(itemDTO.title()).orElseThrow(EntityNotFoundException::new);
            }
            else {
                newItem = itemRepository.findById(itemDTO.itemId()).orElseThrow(EntityNotFoundException::new);
            }
            SchoolItem newSchoolItem = new SchoolItem(itemDTO.schoolId(), itemDTO.tags(), itemDTO.description(), itemDTO.featured(), newItem);
            schoolItemRepository.save(newSchoolItem);
            return mapToSchoolItemDTO(newSchoolItem);
        }
        throw new EntityAlreadyExistsException();
    }

    //Checks if given id has an item that's linked to the school attached. If so deletes it
    public SchoolItemDTO deleteSchoolItem(int id) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        schoolItemRepository.delete(schoolItem);
        return mapToSchoolItemDTO(schoolItem);
    }

    //Gets all items in the database
    public List<ItemDTO> getAllItems() {
        List<Item> items =  itemRepository.findAll();
        List<ItemDTO> itemDTOS = items.stream().map(item -> mapToItemDTO(item)).toList();
        return itemDTOS;
    }

    //Updates the details of a SchoolItem alongside the Item it's linked to.
    public SchoolItemDTO updateSchoolItem(int id, UpdateSchoolItemDTO schoolItemDTO) throws EntityNotFoundException {
        SchoolItem currentSchoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        Item newItem = new Item(schoolItemDTO.title(), schoolItemDTO.publisher(), schoolItemDTO.author(), currentSchoolItem.getItem().getIsbn(), schoolItemDTO.releaseYear(), schoolItemDTO.bookLanguage(), schoolItemDTO.pageCount(), schoolItemDTO.cover(),  currentSchoolItem.getItem().getReviewScore(),  currentSchoolItem.getItem().getReviewCount());
        newItem.setId(currentSchoolItem.getItem().getId());
        SchoolItem newSchoolItem = new SchoolItem(currentSchoolItem.getSchoolId(), schoolItemDTO.tags(), schoolItemDTO.description(), schoolItemDTO.featured(), newItem);

        newSchoolItem.setId(id);
        itemRepository.save(newItem);
        schoolItemRepository.save(newSchoolItem);
        return mapToSchoolItemDTO(newSchoolItem);
    }

    //Gets a SchoolItem by its id
    public SchoolItemDTO getSchoolItemById(int id) throws EntityNotFoundException {
        SchoolItem schoolItem =  schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToSchoolItemDTO(schoolItem);
    }

    //Gets an Item by its id
    public ItemDTO getItemById(int id) throws EntityNotFoundException {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToItemDTO(item);
    }

    //Gets a SchoolItem by its title
    public SchoolItemDTO getSchoolItemByTitle(String title) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findByItemTitle(title).orElseThrow(EntityNotFoundException::new);
        return mapToSchoolItemDTO(schoolItem);
    }

    //Converts a SchoolItem to an SchoolItemDTO
    private SchoolItemDTO mapToSchoolItemDTO(SchoolItem item) {
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
                item.getSchoolId(),
                item.getTags(),
                item.getDescription(),
                item.isFeatured()
        );
    }

    //Converts an Item to an ItemDTO
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
                item.getReviewCount()
        );
    }
}
