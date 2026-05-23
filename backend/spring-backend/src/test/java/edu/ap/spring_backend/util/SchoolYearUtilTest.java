package edu.ap.spring_backend.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchoolYearUtilTest {

    @Test
    void determineSchoolYear_september1_startsNewSchoolYear() {
        assertEquals("2024-2025", SchoolYearUtil.determineSchoolYear(LocalDate.of(2024, 9, 1)));
    }

    @Test
    void determineSchoolYear_august31_belongsToPreviousSchoolYear() {
        assertEquals("2023-2024", SchoolYearUtil.determineSchoolYear(LocalDate.of(2024, 8, 31)));
    }

    @Test
    void determineSchoolYear_january1_midSchoolYear() {
        assertEquals("2024-2025", SchoolYearUtil.determineSchoolYear(LocalDate.of(2025, 1, 1)));
    }

    @Test
    void determineSchoolYear_december31_endOfCalendarYear() {
        assertEquals("2024-2025", SchoolYearUtil.determineSchoolYear(LocalDate.of(2024, 12, 31)));
    }

    @Test
    void determineSchoolYear_leapYearFebruary29_belongsToPreviousSchoolYear() {
        assertEquals("2023-2024", SchoolYearUtil.determineSchoolYear(LocalDate.of(2024, 2, 29)));
    }

    @Test
    void determineSchoolYear_september1LeapYear_startsNewSchoolYear() {
        assertEquals("2024-2025", SchoolYearUtil.determineSchoolYear(LocalDate.of(2024, 9, 1)));
    }
}
