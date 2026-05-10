package edu.ap.spring_backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.LendingDTO;
import edu.ap.spring_backend.dto.LendingRequestDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Lending;
import edu.ap.spring_backend.jpa.User;

@Service
public class LendingService {

    private final LendingRepository lendingRepository;
    private final UserRepository userRepository;
    private final SchoolItemCopyRepository copyRepository;
    private final SchoolRepository schoolRepository;

    public LendingService(LendingRepository repository,
            UserRepository userRepository,
            SchoolItemCopyRepository copyRepository,
            SchoolRepository schoolRepository) {
        this.lendingRepository = repository;
        this.userRepository = userRepository;
        this.copyRepository = copyRepository;
        this.schoolRepository = schoolRepository;
    }

    private String getBorrowerName(Lending lending) {
        if (lending.getBorrower() == null)
            return null;

        User user = lending.getBorrower();

        return user.getFirstName() != null
                ? user.getFirstName() + " " + user.getName()
                : user.getName();
    }

    private Long getItemId(Lending lending) {
        if (lending.getBook() == null || lending.getBook().getItem() == null) {
            return null;
        }
        return lending.getBook().getItem().getId();
    }

    public List<LendingDTO> getAll() {
        return lendingRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public LendingDTO addLending(LendingRequestDTO request) {
        Lending lending = new Lending();

        lending.setBorrower(userRepository.findById(request.borrowerId()).orElseThrow());
        lending.setBook(copyRepository.findByBarcode(request.copyBarcode()).orElseThrow());
        lending.setSchool(schoolRepository.findById(request.schoolId()).orElseThrow());
        lending.setLendingDate(request.lendingDate());
        lending.setReturnDate(request.returnDate());
        lending.setReturned(false);
        lending.setDateReturned(null);
        lending.setExtended(false);

        Lending saved = lendingRepository.save(lending);

        return toDTO(saved);
    }


    public LendingDTO updateLending(Long id, LendingDTO dto) {
        Lending lending = lendingRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        lending.setReturned(dto.returned());
        lending.setDateReturned(
                dto.dateReturned() != null
                        ? java.time.LocalDate.parse(dto.dateReturned().substring(0, 10))
                        : null);

        return toDTO(lendingRepository.save(lending));
    }

    public void deleteLending(Long id) {
        lendingRepository.deleteById(id);
    }

    public LendingDTO getById(Long id) {
        return lendingRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(EntityNotFoundException::new);
    }

    public LendingDTO extendLending(Long id) {
        Lending lending = lendingRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (Boolean.TRUE.equals(lending.getExtended())) {
            throw new IllegalStateException("Lending has already been extended");
        }
        lending.setReturnDate(lending.getReturnDate().plusDays(lending.getSchool().getExtendPeriodDays()));
        lending.setExtended(true);

        return toDTO(lendingRepository.save(lending));
    }

    public List<LendingDTO> getAllByUser(String email) {
        return lendingRepository.findByBorrowerEmail(email).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<LendingDTO> getAllByUserId(Long userId) {
        return lendingRepository.findByBorrower_Id(userId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<LendingDTO> getLendingsByBookId(Long bookId) {
        return lendingRepository.findByBook_Item_Id(bookId).stream()
                .map(this::toDTO)
                .toList();
    }

    private LendingDTO toDTO(Lending lending) {
        return new LendingDTO(
                lending.getId(),
                getBorrowerName(lending),
                lending.getBorrower() != null
                        ? lending.getBorrower().getId().longValue()
                        : null,
                lending.getBook().getItem().getTitle(),
                lending.getBook().getItem().getAuthor(),
                getItemId(lending),
                lending.getSchool().getId(),
                lending.getLendingDate().toString(),
                lending.getReturnDate().toString(),
                lending.getDateReturned() != null
                        ? lending.getDateReturned().toString()
                        : null,
                lending.getReturned(),
                lending.getBook().getBarcode(),
                lending.getExtended());
    }
}