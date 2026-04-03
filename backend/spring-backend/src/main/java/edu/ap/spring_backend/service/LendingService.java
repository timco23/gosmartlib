package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.LendingDTO;
import edu.ap.spring_backend.dto.LendingRequestDTO;
import edu.ap.spring_backend.jpa.Lending;

@Service
public class LendingService {
        private final LendingRepository lendingRepository;
        private final UserRepository userRepository;
        private final SchoolItemCopyRepository copyRepository;
        private final SchoolRepository schoolRepository;

        public LendingService(LendingRepository repository, UserRepository userRepository,
                        SchoolItemCopyRepository copyRepository, SchoolRepository schoolRepository) {
                lendingRepository = repository;
                this.userRepository = userRepository;
                this.copyRepository = copyRepository;
                this.schoolRepository = schoolRepository;
        }

        public List<LendingDTO> getAll() {
                return lendingRepository.findAll().stream()
                                .map(lending -> new LendingDTO(
                                                lending.getId(),
                                                lending.getBorrower() != null
                                                                ? lending.getBorrower().getSmartschoolUsername()
                                                                : null,
                                                lending.getBorrower() != null
                                                                ? lending.getBorrower().getId().longValue()
                                                                : null,
                                                lending.getBook().getItem().getTitle(),
                                                lending.getBook().getItem().getAuthor(),
                                                lending.getSchool().getId(),
                                                lending.getLendingDate().toString(),
                                                lending.getReturnDate().toString(),
                                                lending.getDateReturned() != null ? lending.getDateReturned().toString()
                                                                : null,
                                                lending.getReturned(),
                                                lending.getBook().getBarcode(),
                                                lending.getExtended()))
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

                Lending saved = lendingRepository.save(lending);

                return new LendingDTO(
                                saved.getId(),
                                saved.getBorrower() != null ? saved.getBorrower().getSmartschoolUsername() : null,
                                lending.getBorrower() != null ? lending.getBorrower().getId().longValue() : null,
                                saved.getBook().getItem().getTitle(),
                                saved.getBook().getItem().getAuthor(),
                                saved.getSchool().getId(),
                                saved.getLendingDate().toString(),
                                saved.getReturnDate().toString(),
                                null,
                                false,
                                saved.getBook().getBarcode(),
                                false);
        }

        public LendingDTO updateLending(Long id, LendingDTO dto) {
                Lending lending = lendingRepository.findById(id).orElseThrow();
                lending.setReturned(dto.returned());
                lending.setDateReturned(
                                dto.dateReturned() != null
                                                ? java.time.LocalDate.parse(dto.dateReturned().substring(0, 10))
                                                : null);
                Lending saved = lendingRepository.save(lending);

                return new LendingDTO(
                                saved.getId(),
                                saved.getBorrower() != null ? saved.getBorrower().getSmartschoolUsername() : null,
                                lending.getBorrower() != null ? lending.getBorrower().getId().longValue() : null,
                                saved.getBook().getItem().getTitle(),
                                saved.getBook().getItem().getAuthor(),
                                saved.getSchool().getId(),
                                saved.getLendingDate().toString(),
                                saved.getReturnDate().toString(),
                                saved.getDateReturned() != null ? saved.getDateReturned().toString() : null,
                                saved.getReturned(),
                                saved.getBook().getBarcode(),
                                saved.getExtended());
        }

        public void deleteLending(Long id) {
                lendingRepository.deleteById(id);
        }

        public LendingDTO getById(Long id) {
                return lendingRepository.findById(id)
                                .map(lending -> new LendingDTO(
                                                lending.getId(),
                                                lending.getBorrower() != null
                                                                ? lending.getBorrower().getSmartschoolUsername()
                                                                : null,
                                                lending.getBorrower() != null
                                                                ? lending.getBorrower().getId().longValue()
                                                                : null,
                                                lending.getBook().getItem().getTitle(),
                                                lending.getBook().getItem().getAuthor(),
                                                lending.getSchool().getId(),
                                                lending.getLendingDate().toString(),
                                                lending.getReturnDate().toString(),
                                                lending.getDateReturned() != null ? lending.getDateReturned().toString()
                                                                : null,
                                                lending.getReturned(),
                                                lending.getBook().getBarcode(),
                                                lending.getExtended()))
                                .orElseThrow();
        }

        public LendingDTO extendLending(Long id) {
                Lending lending = lendingRepository.findById(id).orElseThrow();
                if (lending.getExtended()) {
                        throw new IllegalStateException("Lending has already been extended");
                }

                int extendDays = lending.getSchool().getExtendPeriodDays();
                lending.setReturnDate(lending.getReturnDate().plusDays(extendDays));
                lending.setExtended(true);
                Lending saved = lendingRepository.save(lending);

                return new LendingDTO(
                                saved.getId(),
                                saved.getBorrower() != null ? saved.getBorrower().getSmartschoolUsername() : null,
                                saved.getBorrower() != null ? saved.getBorrower().getId().longValue() : null,
                                saved.getBook().getItem().getTitle(),
                                saved.getBook().getItem().getAuthor(),
                                saved.getSchool().getId(),
                                saved.getLendingDate().toString(),
                                saved.getReturnDate().toString(),
                                saved.getDateReturned() != null ? saved.getDateReturned().toString() : null,
                                saved.getReturned(),
                                saved.getBook().getBarcode(),
                                saved.getExtended());
        }
}
