package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificationServiceTest {

    @Mock
    private LendingRepository lendingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SmartschoolNotificationService notificationService;

    @InjectMocks
    private ScheduledNotificationService scheduledService;

    private School school;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setLenderNotificationPeriod(3);
        school.setLenderLateNotificationPeriod(2);
        school.setLibrarianNotificationNotReturned(5);
        school.setMaxExtends(2);
        school.setMaxLendsStudent(5);
        school.setMaxLendTeacher(10);
    }

    @Test
    void runDailyNotifications_dueInExactlyNotificationPeriod_shouldNotifyBorrower() {
        LocalDate returnDate = LocalDate.now().plusDays(3); // lenderNotificationPeriod = 3
        Lending lending = buildLending(returnDate, buildStudent("jan.peeters"));

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verify(notificationService).sendNotificationAndMessage(
                eq(lending.getBorrower()),
                contains("Herinnering"),
                anyString(),
                isNull(),
                anyString(),
                anyString());
    }

    @Test
    void runDailyNotifications_dueInWrongNumberOfDays_shouldNotNotifyBorrower() {
        LocalDate returnDate = LocalDate.now().plusDays(5); // not equal to lenderNotificationPeriod (3)
        Lending lending = buildLending(returnDate, buildStudent("jan.peeters"));

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verify(notificationService, never()).sendNotificationAndMessage(
                any(), contains("Herinnering"), anyString(), any(), anyString(), anyString());
    }

    @Test
    void runDailyNotifications_overdueExactlyLateNotificationPeriod_shouldNotifyBorrower() {
        LocalDate returnDate = LocalDate.now().minusDays(2); // lenderLateNotificationPeriod = 2
        Lending lending = buildLending(returnDate, buildStudent("jan.peeters"));

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verify(notificationService).sendNotificationAndMessage(
                eq(lending.getBorrower()),
                contains("Te laat"),
                anyString(),
                isNull(),
                anyString(),
                anyString());
    }

    @Test
    void runDailyNotifications_overdue_studentBorrower_shouldAlsoNotifyParents() {
        LocalDate returnDate = LocalDate.now().minusDays(2);
        User student = buildStudent("jan.peeters");
        Lending lending = buildLending(returnDate, student);

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verify(notificationService).sendNotificationAndMessage(
                eq(student),
                contains("Te laat"),
                anyString(),
                isNull(),
                anyString(),
                contains("ouders/voogd"));
    }

    @Test
    void runDailyNotifications_overdue_teacherBorrower_shouldNotNotifyParents() {
        LocalDate returnDate = LocalDate.now().minusDays(2);
        User teacher = new User();
        teacher.setRole(Role.LEERKRACHT);
        teacher.setSmartschoolUsername("marie.janssen");
        teacher.setName("Janssen");
        teacher.setFirstName("Marie");
        Lending lending = buildLending(returnDate, teacher);

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verify(userRepository, never()).findBySmartschoolUsernameStartingWith(anyString());
    }

    @Test
    void runDailyNotifications_overdueExactlyLibrarianPeriod_shouldNotifyLibrarians() {
        LocalDate returnDate = LocalDate.now().minusDays(5); // librarianNotificationNotReturned = 5
        Lending lending = buildLending(returnDate, buildStudent("jan.peeters"));

        User librarian = new User();
        librarian.setRole(Role.BIBLIOTHEEKBEHEERDER);
        librarian.setSmartschoolUsername("lib.beheerder");
        librarian.setEmail("lib@school.be");

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));
        when(userRepository.findBySchool_IdAndRoleIn(any(), anyList()))
                .thenReturn(List.of(librarian));

        scheduledService.runDailyNotifications();

        verify(notificationService).sendNotificationAndMessage(
                eq(librarian),
                contains("Niet-teruggebracht"),
                anyString(),
                isNull(),
                anyString(),
                anyString());
    }

    @Test
    void runDailyNotifications_overdueButNotYetLibrarianPeriod_shouldNotNotifyLibrarians() {
        LocalDate returnDate = LocalDate.now().minusDays(3); // not yet 5 days
        Lending lending = buildLending(returnDate, buildStudent("jan.peeters"));

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verify(userRepository, never()).findBySchool_IdAndRoleIn(any(), anyList());
    }

    @Test
    void runDailyNotifications_noActiveLendings_shouldDoNothing() {
        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of());

        scheduledService.runDailyNotifications();

        verifyNoInteractions(notificationService);
    }

    @Test
    void runDailyNotifications_lendingWithNullSchool_shouldSkip() {
        Lending lending = new Lending();
        lending.setSchool(null);
        lending.setReturnDate(LocalDate.now().plusDays(3));

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verifyNoInteractions(notificationService);
    }

    @Test
    void runDailyNotifications_lendingWithNullReturnDate_shouldSkip() {
        Lending lending = new Lending();
        lending.setSchool(school);
        lending.setReturnDate(null);

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verifyNoInteractions(notificationService);
    }

    @Test
    void runDailyNotifications_schoolWithZeroPeriods_shouldNeverNotify() {
        School silentSchool = new School();
        silentSchool.setLenderNotificationPeriod(0);
        silentSchool.setLenderLateNotificationPeriod(0);
        silentSchool.setLibrarianNotificationNotReturned(0);

        Lending lending = buildLending(LocalDate.now().minusDays(10), buildStudent("jan.peeters"));
        lending.setSchool(silentSchool);

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(lending));

        scheduledService.runDailyNotifications();

        verifyNoInteractions(notificationService);
    }

    @Test
    void runDailyNotifications_multipleLendings_eachHandledIndependently() {
        // Lending 1: due reminder
        Lending reminder = buildLending(LocalDate.now().plusDays(3), buildStudent("user.a"));
        // Lending 2: late notice
        Lending late = buildLending(LocalDate.now().minusDays(2), buildStudent("user.b"));

        when(lendingRepository.findByReturnedFalse()).thenReturn(List.of(reminder, late));

        scheduledService.runDailyNotifications();

        verify(notificationService).sendNotificationAndMessage(
                eq(reminder.getBorrower()),
                contains("Herinnering"),
                anyString(),
                isNull(),
                anyString(),
                anyString());

        verify(notificationService).sendNotificationAndMessage(
                eq(late.getBorrower()),
                contains("Te laat"),
                anyString(),
                isNull(),
                anyString(),
                anyString());
    }

    private Lending buildLending(LocalDate returnDate, User borrower) {
        Item item = new Item();
        try {
            Field titleField = Item.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(item, "Testboek");
        } catch (Exception ignored) {}

        SchoolItemCopy copy = new SchoolItemCopy();
        try {
            Field itemField = SchoolItemCopy.class.getDeclaredField("item");
            itemField.setAccessible(true);
            itemField.set(copy, item);
            Field barcodeField = SchoolItemCopy.class.getDeclaredField("barcode");
            barcodeField.setAccessible(true);
            barcodeField.set(copy, "BC000001");
        } catch (Exception ignored) {}

        Lending lending = new Lending();
        lending.setSchool(school);
        lending.setReturnDate(returnDate);
        lending.setBorrower(borrower);
        lending.setBook(copy);
        lending.setReturned(false);
        return lending;
    }

    private User buildStudent(String username) {
        User user = new User();
        user.setRole(Role.LEERLING);
        user.setSmartschoolUsername(username);
        user.setEmail(username + "@school.be");
        user.setName(username.split("\\.")[1]);
        user.setFirstName(username.split("\\.")[0]);
        return user;
    }
}