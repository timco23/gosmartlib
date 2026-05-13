package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.BookStatisticsDTO;
import edu.ap.spring_backend.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/book/{bookId}/statistics")
    public ResponseEntity<BookStatisticsDTO> getBookStatistics(
            @PathVariable Long bookId,
            @RequestParam Long schoolId,
            @RequestParam(defaultValue = "month") String groupBy,
            @RequestParam(defaultValue = "6") int trendLimit) {
        return ResponseEntity.ok(statisticsService.getBookStatistics(bookId, schoolId, groupBy, trendLimit));
    }
}
