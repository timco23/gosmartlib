package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dto.LendingDTO;
import edu.ap.spring_backend.jpa.Lending;

@Service
public class LendingService {
    private final LendingRepository lendingRepository;

    public LendingService(LendingRepository repository){
        lendingRepository = repository;
    }

    public List<LendingDTO> getAll(){
        return lendingRepository.findAll().stream()
            .map(lending -> new LendingDTO(
                lending.getId(),
                lending.getBorrower() != null ? lending.getBorrower().getSmartschoolUsername() : null,
                lending.getBook().getItem().getTitle(),
                lending.getBook().getItem().getAuthor(),
                lending.getSchool().getId(),
                lending.getLendingDate().toString(),
                lending.getReturnDate().toString(),
                lending.getDateReturned() != null ? lending.getDateReturned().toString() : null,
                lending.getReturned()
            )).toList();
    }

    public Lending addLending(Lending newLending){
        return lendingRepository.save(newLending);
    }

    public Lending updateLending(Long id, Lending updatedLending){
        Lending lending = lendingRepository.findById(id).orElseThrow();
        lending.setBorrower(updatedLending.getBorrower());
        lending.setBook(updatedLending.getBook());
        lending.setSchool(updatedLending.getSchool());
        lending.setLendingDate(updatedLending.getLendingDate());
        lending.setReturnDate(updatedLending.getReturnDate());
        lending.setReturned(updatedLending.getReturned());
        lending.setDateReturned(updatedLending.getDateReturned());
        return lendingRepository.save(lending);
    }

    public void deleteLending(Long id){
        lendingRepository.deleteById(id);
    }

    public LendingDTO getById(Long id){
        return lendingRepository.findById(id)
            .map(lending -> new LendingDTO(
                lending.getId(),
                lending.getBorrower() != null ? lending.getBorrower().getSmartschoolUsername() : null,
                lending.getBook().getItem().getTitle(),
                lending.getBook().getItem().getAuthor(),
                lending.getSchool().getId(),
                lending.getLendingDate().toString(),
                lending.getReturnDate().toString(),
                lending.getDateReturned() != null ? lending.getDateReturned().toString() : null,
                lending.getReturned()
            )).orElseThrow();
    }
}
