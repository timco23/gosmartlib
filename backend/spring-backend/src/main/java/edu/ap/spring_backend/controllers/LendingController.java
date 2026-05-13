package edu.ap.spring_backend.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.BookStatisticsDTO;
import edu.ap.spring_backend.dto.CampusAvailabilityDTO;
import edu.ap.spring_backend.dto.LendingDTO;
import edu.ap.spring_backend.dto.LendingRequestDTO;
import edu.ap.spring_backend.service.LendingService;

@RestController
@RequestMapping("/lendings")
public class LendingController {

    private final LendingService lendingService;

    public LendingController(LendingService lendingService) {
        this.lendingService = lendingService;
    }

    @GetMapping()
    public ResponseEntity<Object> getAllLendings() {
        return ResponseEntity.ok(lendingService.getAll());
    }

    @GetMapping("/user")
    public ResponseEntity<Object> getAllLendingsByUser(@RequestParam String email) {
        return ResponseEntity.ok(lendingService.getAllByUser(email));
    }

    @GetMapping("/userId")
    public ResponseEntity<Object> getAllLendingsByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(lendingService.getAllByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getLendingById(@PathVariable Long id) {
        return ResponseEntity.ok(lendingService.getById(id));
    }

    @GetMapping("/school/{id}")
    public ResponseEntity<Object> getLendingBySchool(@PathVariable Long id) {
        return ResponseEntity.ok(lendingService.getBySchool(id));
    }

    @PostMapping()
    public ResponseEntity<LendingDTO> addLending(@RequestBody LendingRequestDTO newLending) {
        return ResponseEntity.status(201).body(lendingService.addLending(newLending));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLending(@PathVariable Long id) {
        lendingService.deleteLending(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<LendingDTO> updateLending(@PathVariable Long id, @RequestBody LendingDTO dto) {
        return ResponseEntity.ok(lendingService.updateLending(id, dto));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<LendingDTO> extendLending(@PathVariable Long id) {
        return ResponseEntity.ok(lendingService.extendLending(id));
    }


    @GetMapping("/book/{bookId}")
    public ResponseEntity<Object> getLendingsByBookId(@PathVariable Long bookId) {
        return ResponseEntity.ok(lendingService.getLendingsByBookId(bookId));
    }

    @GetMapping("/book/{bookId}/earliest-availability")
    public ResponseEntity<List<CampusAvailabilityDTO>> getEarliestAvailabilityPerCampus(@PathVariable Long bookId) {
        return ResponseEntity.ok(lendingService.getEarliestAvailabilityPerCampusByBookId(bookId));
    }

    @GetMapping("/most-borrowed-this-month/{schoolId}")
    public ResponseEntity<ItemDTO> getMostBorrowedThisMonth(@PathVariable Long schoolId) {
        return lendingService.getMostBorrowedThisMonth(schoolId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/last-borrowed/{email}")
    public ResponseEntity<ItemDTO> getLastBorrowedBook(@PathVariable String email) {
        return lendingService.getLastBorrowedBook(email)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }
}