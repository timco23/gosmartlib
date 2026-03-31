package edu.ap.spring_backend.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ap.spring_backend.jpa.Lending;
import edu.ap.spring_backend.service.LendingService;

@RestController
@RequestMapping("/lendings")
public class LendingController {
    public LendingService lendingService;

    public LendingController(LendingService lendingService) {
        this.lendingService = lendingService;
    }

    @GetMapping()
    public Object getAllLendings() {
        return lendingService.getAll();
    }

    @GetMapping("/{id}")
    public Object getLendingById(Long id) {
        return lendingService.getById(id);
    }

    @PostMapping()
    public Object addLending(@RequestBody Lending newLending) {
        return lendingService.addLending(newLending);
    }

    @DeleteMapping("/{id}")
    public void deleteLending(@PathVariable Long id) {
        lendingService.deleteLending(id);
    }
}
