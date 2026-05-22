package edu.ap.spring_backend.util;

import java.time.LocalDate;
import java.time.Month;

public final class SchoolYearUtil {

    private SchoolYearUtil() {}

    /**
     * Returns the school year for the given date in the format "YYYY-YYYY+1".
     */
    public static String determineSchoolYear(LocalDate date) {
        int year = date.getMonth().compareTo(Month.SEPTEMBER) >= 0
                ? date.getYear()
                : date.getYear() - 1;
        return year + "-" + (year + 1);
    }
}
