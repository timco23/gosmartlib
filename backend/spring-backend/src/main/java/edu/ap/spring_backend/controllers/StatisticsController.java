package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.BookStatisticsDTO;
import edu.ap.spring_backend.dto.ClassStatisticsDTO;
import edu.ap.spring_backend.dto.SchoolStatisticsDTO;
import edu.ap.spring_backend.dto.StudentStatisticsDTO;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.service.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

    public StatisticsController(StatisticsService statisticsService, UserRepository userRepository) {
        this.statisticsService = statisticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<StudentStatisticsDTO> getOwnStatistics(
            Authentication auth,
            @RequestParam(required = false) String schoolYear) {
        User user = resolveUser(auth);
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No school linked to this user");
        }
        return ResponseEntity.ok(statisticsService.getStudentStatistics(user.getId(), user.getSchool().getId(), schoolYear));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<BookStatisticsDTO> getBookStatistics(
            @PathVariable Long bookId,
            @RequestParam Long schoolId,
            @RequestParam(defaultValue = "month") String groupBy,
            @RequestParam(defaultValue = "6") int trendLimit) {
        return ResponseEntity.ok(statisticsService.getBookStatistics(bookId, schoolId, groupBy, trendLimit));
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<StudentStatisticsDTO> getStudentStatistics(
            @PathVariable Integer userId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) String schoolYear) {
        return ResponseEntity.ok(statisticsService.getStudentStatistics(userId, schoolId, schoolYear));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ClassStatisticsDTO> getClassStatistics(
            @PathVariable Long classId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) String schoolYear) {
        return ResponseEntity.ok(statisticsService.getClassStatistics(classId, schoolId, schoolYear));
    }

    @GetMapping("/school-years")
    public ResponseEntity<List<String>> getAvailableSchoolYears(@RequestParam Long schoolId) {
        return ResponseEntity.ok(statisticsService.getAvailableSchoolYears(schoolId));
    }

    @GetMapping("/school")
    public ResponseEntity<SchoolStatisticsDTO> getSchoolStatistics(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String schoolYear) {
        return ResponseEntity.ok(statisticsService.getSchoolStatistics(schoolId, schoolYear));
    }

    private User resolveUser(Authentication auth) {
        try {
            Integer userId = Integer.valueOf(auth.getName());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }
}
