package edu.ap.spring_backend.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.BookStatisticsDTO;
import edu.ap.spring_backend.dto.CampusAvailabilityDTO;
import edu.ap.spring_backend.dto.LendingDTO;
import edu.ap.spring_backend.dto.LendingRequestDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.Lending;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
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

    public BookStatisticsDTO getBookStatistics(Long bookId, Long schoolId) {
        List<Lending> lendings = lendingRepository.findByBook_Item_IdAndSchool_Id(bookId, schoolId);
        List<SchoolItemCopy> copies = copyRepository.findByItemIdAndSchoolId(bookId, schoolId);
        LocalDate today = LocalDate.now();

        int totalLendings = lendings.size();
        long activeCount = lendings.stream().filter(l -> !Boolean.TRUE.equals(l.getReturned())).count();
        int activeLendings = (int) activeCount;

        List<Lending> returnedWithDates = lendings.stream()
                .filter(l -> Boolean.TRUE.equals(l.getReturned()) && l.getDateReturned() != null)
                .toList();
        int avgDurationDays = returnedWithDates.isEmpty() ? 0 : (int) Math.round(
                returnedWithDates.stream()
                        .mapToLong(l -> ChronoUnit.DAYS.between(l.getLendingDate(), l.getDateReturned()))
                        .average().orElse(0));

        long lateCount = lendings.stream().filter(l -> {
            if (l.getDateReturned() != null) return l.getDateReturned().isAfter(l.getReturnDate());
            return !Boolean.TRUE.equals(l.getReturned()) && today.isAfter(l.getReturnDate());
        }).count();
        int lateReturnPercent = totalLendings == 0 ? 0 : (int) Math.round(100.0 * lateCount / totalLendings);

        long extendedCount = lendings.stream().filter(l -> Boolean.TRUE.equals(l.getExtended())).count();
        int extensionPercent = totalLendings == 0 ? 0 : (int) Math.round(100.0 * extendedCount / totalLendings);

        int globalUnavailableDays = 0;
        int totalCopies = copies.size();
        if (totalCopies > 0 && !lendings.isEmpty()) {
            LocalDate start = lendings.stream().map(Lending::getLendingDate).min(LocalDate::compareTo).orElse(today);
            LocalDate d = start;
            while (!d.isAfter(today)) {
                final LocalDate date = d;
                long active = lendings.stream().filter(l -> {
                    LocalDate s = l.getLendingDate();
                    LocalDate e = l.getDateReturned() != null ? l.getDateReturned()
                            : Boolean.TRUE.equals(l.getReturned()) ? l.getReturnDate() : today;
                    return !date.isBefore(s) && !date.isAfter(e);
                }).count();
                if (active >= totalCopies) globalUnavailableDays++;
                d = d.plusDays(1);
            }
        }

        Map<String, Long> monthMap = lendings.stream().collect(Collectors.groupingBy(
                l -> l.getLendingDate().getYear() + "-" + String.format("%02d", l.getLendingDate().getMonthValue()),
                Collectors.counting()));
        List<BookStatisticsDTO.MonthlyLendingDTO> lendingsByMonth = monthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new BookStatisticsDTO.MonthlyLendingDTO(e.getKey(), e.getValue().intValue()))
                .collect(Collectors.toList());
        if (lendingsByMonth.size() > 6) {
            lendingsByMonth = lendingsByMonth.subList(lendingsByMonth.size() - 6, lendingsByMonth.size());
        }

        Map<String, List<SchoolItemCopy>> campusMap = copies.stream()
                .collect(Collectors.groupingBy(c -> c.getCampus() != null ? c.getCampus().getName() : "Geen locatie"));
        List<BookStatisticsDTO.CampusStatisticsDTO> campusStats = campusMap.entrySet().stream()
                .map(entry -> {
                    List<SchoolItemCopy> campusCopies = entry.getValue();
                    Set<String> barcodes = campusCopies.stream().map(SchoolItemCopy::getBarcode)
                            .collect(Collectors.toSet());
                    List<Lending> campusLendings = lendings.stream()
                            .filter(l -> barcodes.contains(l.getBook().getBarcode())).toList();

                    int campusTotal = campusLendings.size();
                    int campusActive = (int) campusLendings.stream()
                            .filter(l -> !Boolean.TRUE.equals(l.getReturned())).count();
                    List<Lending> campusReturned = campusLendings.stream()
                            .filter(l -> Boolean.TRUE.equals(l.getReturned()) && l.getDateReturned() != null).toList();
                    int campusAvgDuration = campusReturned.isEmpty() ? 0 : (int) Math.round(
                            campusReturned.stream()
                                    .mapToLong(l -> ChronoUnit.DAYS.between(l.getLendingDate(), l.getDateReturned()))
                                    .average().orElse(0));
                    double lendingsPerCopy = campusCopies.isEmpty() ? 0
                            : Math.round((double) campusTotal / campusCopies.size() * 10.0) / 10.0;
                    int campusAvailableCopies = (int) campusCopies.stream()
                            .filter(c -> c.getCopy_status() == CopyStatus.BESCHIKBAAR).count();

                    int campusUnavailableDays = 0;
                    if (!campusLendings.isEmpty()) {
                        LocalDate campusStart = campusLendings.stream().map(Lending::getLendingDate)
                                .min(LocalDate::compareTo).orElse(today);
                        LocalDate d = campusStart;
                        while (!d.isAfter(today)) {
                            final LocalDate date = d;
                            long active = campusLendings.stream().filter(l -> {
                                LocalDate s = l.getLendingDate();
                                LocalDate e = l.getDateReturned() != null ? l.getDateReturned()
                                        : Boolean.TRUE.equals(l.getReturned()) ? l.getReturnDate() : today;
                                return !date.isBefore(s) && !date.isAfter(e);
                            }).count();
                            if (active >= campusCopies.size()) campusUnavailableDays++;
                            d = d.plusDays(1);
                        }
                    }

                    return new BookStatisticsDTO.CampusStatisticsDTO(entry.getKey(), campusCopies.size(),
                            campusAvailableCopies, campusTotal, campusActive, campusAvgDuration,
                            lendingsPerCopy, campusUnavailableDays);
                })
                .sorted(Comparator.comparingInt(BookStatisticsDTO.CampusStatisticsDTO::lendingCount).reversed())
                .toList();

        List<BookStatisticsDTO.RecentLendingDTO> recentLendings = lendings.stream()
                .sorted(Comparator.comparing(Lending::getLendingDate).reversed())
                .limit(8)
                .map(l -> new BookStatisticsDTO.RecentLendingDTO(
                        l.getId(), getBorrowerName(l),
                        l.getLendingDate().toString(), l.getReturnDate().toString(),
                        l.getDateReturned() != null ? l.getDateReturned().toString() : null,
                        Boolean.TRUE.equals(l.getReturned()),
                        Boolean.TRUE.equals(l.getExtended())))
                .toList();

        return new BookStatisticsDTO(totalLendings, activeLendings, avgDurationDays, lateReturnPercent,
                extensionPercent, globalUnavailableDays, lendingsByMonth, campusStats, recentLendings);
    }

    public List<CampusAvailabilityDTO> getEarliestAvailabilityPerCampusByBookId(Long bookId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.of("nl", "BE"));
        Map<String, LocalDate> earliest = new HashMap<>();

        for (Lending lending : lendingRepository.findByBook_Item_IdAndReturnedFalse(bookId)) {
            String campus = lending.getBook().getCampus().getName();
            LocalDate date = lending.getReturnDate();
            if (!earliest.containsKey(campus) || date.isBefore(earliest.get(campus))) {
                earliest.put(campus, date);
            }
        }
        return earliest.entrySet().stream()
                .map(e -> new CampusAvailabilityDTO(e.getKey(), e.getValue().format(formatter)))
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