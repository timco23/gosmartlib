package edu.ap.spring_backend.service;

import edu.ap.spring_backend.DAO.ItemRepository;
import edu.ap.spring_backend.DAO.SchoolItemRepository;
import edu.ap.spring_backend.dto.CreateSchoolItemDTO;
import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.SchoolItemDTO;
import edu.ap.spring_backend.dto.UpdateSchoolItemDTO;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolItem;
import edu.ap.spring_backend.exception.EntityAlreadyExistsException;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.DAO.SchoolRepository;
import org.springframework.stereotype.Service;
import edu.ap.spring_backend.DAO.CampusRepository;

import java.util.List;

@Service
public class ItemService {
    private final SchoolItemRepository schoolItemRepository;
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    public ItemService(SchoolItemRepository schoolItemRepository, ItemRepository itemRepository,  SchoolRepository schoolRepository, CampusRepository campusRepository) {
        this.schoolItemRepository = schoolItemRepository;
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
    }

    // Gets all SchoolItems
    public List<SchoolItemDTO> getAllSchoolItems(){
        List<SchoolItem> schoolItems = schoolItemRepository.findAll();
        List<SchoolItemDTO> schoolItemDTOS = schoolItems.stream().map(item -> mapToSchoolItemDTO(item)).toList();

        return schoolItemDTOS;
    }

    //Adds a SchoolItem. Checks first if school already has this SchoolItem, if not then if the Item isn't already in the database, adds it there first before linking it to a school.
    public SchoolItemDTO addSchoolItem(CreateSchoolItemDTO itemDTO) throws EntityNotFoundException, EntityAlreadyExistsException {
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
            School school = schoolRepository.findById((long) itemDTO.schoolId()).orElseThrow(EntityNotFoundException::new);
            Campus campus = campusRepository.findById((long) itemDTO.campusId()).orElse(null);
            SchoolItem newSchoolItem = new SchoolItem(school, itemDTO.tags(), itemDTO.description(), itemDTO.featured(), newItem, campus);
            schoolItemRepository.save(newSchoolItem);
            return mapToSchoolItemDTO(newSchoolItem);
        }
        throw new EntityAlreadyExistsException();
    }

    //Checks if given id has a SchoolItem attached. If so deletes it
    public SchoolItemDTO deleteSchoolItem(int id) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        schoolItemRepository.delete(schoolItem);
        return mapToSchoolItemDTO(schoolItem);
    }

    //Gets all Items
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
        Campus newCampus = campusRepository.findById((long) schoolItemDTO.campusId()).orElse(null);
        SchoolItem newSchoolItem = new SchoolItem(currentSchoolItem.getSchool(), schoolItemDTO.tags(), schoolItemDTO.description(), schoolItemDTO.featured(), newItem, newCampus);

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
    public SchoolItemDTO getSchoolItemByTitle(String title){
        SchoolItem schoolItem = schoolItemRepository.findByItemTitle(title).orElse(null);
        if (schoolItem != null){
            return mapToSchoolItemDTO(schoolItem);
        }
        return null;
    }

    //Converts a SchoolItem to a SchoolItemDTO
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
                item.getSchool().getId().intValue(),
                item.getTags(),
                item.getDescription(),
                item.isFeatured(),
                item.getCampus() != null ? item.getCampus().getName() : null
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
