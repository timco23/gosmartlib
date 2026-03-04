package edu.ap.spring_backend.service;

import edu.ap.spring_backend.DAO.ItemRepository;
import edu.ap.spring_backend.DAO.SchoolItemRepository;
import edu.ap.spring_backend.controllers.ItemDTO;
import edu.ap.spring_backend.entity.Item;
import edu.ap.spring_backend.entity.SchoolItem;
import edu.ap.spring_backend.exceptions.EntityAlreadyExistsException;
import edu.ap.spring_backend.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private SchoolItemRepository schoolItemRepository;
    private ItemRepository itemRepository;
    public ItemService(SchoolItemRepository schoolItemRepository, ItemRepository itemRepository) {
        this.schoolItemRepository = schoolItemRepository;
        this.itemRepository = itemRepository;
    }

    // Gets all items that a school has
    public List<SchoolItem> getAllSchoolItems(){
        return schoolItemRepository.findAll();
    }

    //Adds item under a school. Checks first if school already has this item, if not then if the item isn't already in the database, adds it there first before linking it to a school.
    public SchoolItem addItem(ItemDTO itemDTO) throws EntityNotFoundException, EntityAlreadyExistsException {
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
            return schoolItemRepository.save(newSchoolItem);
        }
        throw new EntityAlreadyExistsException();
    }

    //Checks if given id has an item that's linked to the school attached. If so deletes it
    public SchoolItem deleteSchoolItem(int id) throws EntityNotFoundException {
        SchoolItem schoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        schoolItemRepository.delete(schoolItem);
        return schoolItem;
    }

    //Gets all items in the database
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    //Updates the details of a book that a school has alongside the book in the database.
    public SchoolItem updateSchoolItem(int id, ItemDTO itemDTO) throws EntityNotFoundException {
        SchoolItem currentSchoolItem = schoolItemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        Item newItem = new Item(itemDTO.title(), itemDTO.publisher(), itemDTO.author(), itemDTO.isbn(), itemDTO.releaseYear(), itemDTO.bookLanguage(), itemDTO.pageCount(), itemDTO.cover(), itemDTO.reviewScore(), itemDTO.reviewCount());
        SchoolItem newSchoolItem = new SchoolItem(itemDTO.schoolId(), itemDTO.tags(), itemDTO.description(), itemDTO.featured(), newItem);

        newItem.setId(currentSchoolItem.getItem().getId());
        newSchoolItem.setId(id);
        itemRepository.save(newItem);
        return schoolItemRepository.save(newSchoolItem);
    }
}
