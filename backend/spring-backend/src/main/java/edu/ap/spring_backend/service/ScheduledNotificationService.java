package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.Lending;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Runs daily at 08:00 to send Smartschool notifications based on school settings.
 * HOW PARENTS ARE REACHED:
 * Smartschool co-accounts (parents) share the same username as their child.
 * When sendmsg is called for a username, Smartschool delivers the message to
 * the main account AND all co-accounts automatically. 
 */
@Service
@EnableScheduling
public class ScheduledNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledNotificationService.class);
        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final LendingRepository lendingRepository;
    private final UserRepository userRepository;
    private final SmartschoolNotificationService notificationService;

    public ScheduledNotificationService(LendingRepository lendingRepository,
                                        UserRepository userRepository,
                                        SmartschoolNotificationService notificationService) {
        this.lendingRepository   = lendingRepository;
        this.userRepository      = userRepository;
        this.notificationService = notificationService;
    }

    // Every day at 08:00

    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyNotifications() {
        log.info("Running daily Smartschool notification job");
        LocalDate today = LocalDate.now();

        List<Lending> active = lendingRepository.findByReturnedFalse();

        for (Lending lending : active) {
            School school = lending.getSchool();
            if (school == null) continue;

            LocalDate returnDate = lending.getReturnDate();
            if (returnDate == null) continue;

            User borrower = lending.getBorrower();

            long daysUntilDue = today.until(returnDate).getDays(); 
            long daysOverdue  = returnDate.until(today).getDays();

            // 1. Reminder before due date → student (notif+msg) + parents (msg via same sendmsg)
            if (school.getLenderNotificationPeriod() > 0
                    && daysUntilDue == school.getLenderNotificationPeriod()) {
                sendReminder(borrower, lending, (int) daysUntilDue);
            }

            // 2. Late notice → student (notif+msg) + parents (msg via same sendmsg)
            if (school.getLenderLateNotificationPeriod() > 0
                    && daysOverdue == school.getLenderLateNotificationPeriod()) {
                sendLateNotice(borrower, lending, (int) daysOverdue);
            }

            // 3. Librarian alert → librarians and admins only
            if (school.getLibrarianNotificationNotReturned() > 0
                    && daysOverdue == school.getLibrarianNotificationNotReturned()) {
                sendLibrarianAlert(lending, school, (int) daysOverdue);
            }
        }

        log.info("Daily notification job complete");
    }

    /**
     * Reminder before due date.
     * sendNotification → student only (bell icon)
     * sendMessage      → student + parents
     */
    private void sendReminder(User borrower, Lending lending, int daysLeft) {
        if (borrower == null) return;

        String bookTitle = lending.getBook().getItem().getTitle();
        String notifTitle = "Herinnering: boek inleveren";
        String notifDesc  = String.format("“%s” moet over %d dag(en) ingeleverd worden.", bookTitle, daysLeft);

        boolean isStudent = borrower.getRole() == Role.LEERLING;
        String msgBody = isStudent
                ? buildReminderBodyForStudent(borrower, bookTitle, daysLeft, lending.getReturnDate())
                : buildReminderBodyForTeacher(borrower, bookTitle, daysLeft, lending.getReturnDate());

        notificationService.sendNotificationAndMessage(
                borrower,
                notifTitle, notifDesc, "/dashboard",
                notifTitle, msgBody);
    }

    /**
     * Late notice after due date.
     * sendNotification → student only (bell icon)
     * sendMessage      → student + parents
     */
    private void sendLateNotice(User borrower, Lending lending, int daysOverdue) {
        if (borrower == null) return;

        String bookTitle  = lending.getBook().getItem().getTitle();
        String notifTitle = "Te laat: boek inleveren";
        String notifDesc  = String.format("'%s' had %d dag(en) geleden ingeleverd moeten worden.",
                bookTitle, daysOverdue);

        boolean isStudent = borrower.getRole() == Role.LEERLING;

        if (isStudent) {
            // Message for student — Smartschool automatically delivers to parents (co-accounts)
            String msgBody = buildLateNoticeBodyForStudent(borrower, bookTitle, daysOverdue, lending.getReturnDate());
            notificationService.sendNotificationAndMessage(
                    borrower,
                    notifTitle, notifDesc, "/dashboard",
                    notifTitle, msgBody);
        } else {
            // Teacher/librarian: notification + message, no parent involvement
            String msgBody = buildLateNoticeBodyForTeacher(borrower, bookTitle, daysOverdue, lending.getReturnDate());
            notificationService.sendNotificationAndMessage(
                    borrower,
                    notifTitle, notifDesc, "/dashboard",
                    notifTitle, msgBody);
        }
    }

    /**
     * Alert to librarians when a book is not returned after X days.
     */
    private void sendLibrarianAlert(Lending lending, School school, int daysOverdue) {
        List<User> librarians = userRepository.findBySchool_IdAndRoleIn(
                school.getId(),
                List.of(Role.BIBLIOTHEEKBEHEERDER, Role.ADMIN));

        String bookTitle = lending.getBook().getItem().getTitle();
        String barcode   = lending.getBook().getBarcode();
        String borrower  = displayName(lending.getBorrower());

        for (User librarian : librarians) {
            String title   = "Niet-teruggebracht boek";
            String desc    = String.format("“%s” — %s — %d dagen te laat.", bookTitle, borrower, daysOverdue);
            String msgBody = String.format(
                    "Beste bibliotheekbeheerder,\n" +
                    "Het boek “%s” (barcode: %s) geleend door %s is %d dag(en) te laat " +
                    "(uiterste datum: %s).\n" +
                    "Gelieve actie te ondernemen.\n" +
                    "Met vriendelijke groeten,\n" +
                    "De bibliotheek GoSmartLib",
                    bookTitle, barcode, borrower, daysOverdue, formatDate(lending.getReturnDate()));

            notificationService.sendNotificationAndMessage(
                    librarian,
                    title, desc, "/lendings",
                    title, msgBody);
        }
    }

    private String buildReminderBodyForStudent(User borrower, String bookTitle, int daysLeft, LocalDate returnDate) {
        return String.format(
                "Beste %s en ouders/voogd,\n" +
                "Dit is een herinnering dat het boek “%s” uiterlijk op %s ingeleverd moet worden " +
                "(nog %d dag(en)).\n" +
                "Gelieve het boek tijdig terug te brengen naar de bibliotheek.\n" +
                "Met vriendelijke groeten,\n" +
                "De bibliotheek GoSmartLib",
                displayName(borrower), bookTitle, formatDate(returnDate), daysLeft);
    }

    private String buildReminderBodyForTeacher(User borrower, String bookTitle, int daysLeft, LocalDate returnDate) {
        return String.format(
                "Beste %s,\n" +
                "Dit is een herinnering dat het boek “%s” uiterlijk op %s ingeleverd moet worden " +
                "(nog %d dag(en)).\n" +
                "Gelieve het boek tijdig terug te brengen naar de bibliotheek.\n" +
                "Met vriendelijke groeten,\n" +
                "De bibliotheek GoSmartLib",
                displayName(borrower), bookTitle, formatDate(returnDate), daysLeft);
    }

    private String buildLateNoticeBodyForStudent(User borrower, String bookTitle, int daysOverdue, LocalDate returnDate) {
        return String.format(
                "Beste %s en ouders/voogd,\n" +
                "Ons systeem geeft aan dat het boek “%s” sinds %d dag(en) te laat is. " +
                "Uiterste datum: %s.\n" +
                "Gelieve het boek zo snel mogelijk terug te brengen naar de bibliotheek.\n" +
                "Met vriendelijke groeten,\n" +
                "De bibliotheek GoSmartLib",
                displayName(borrower), bookTitle, daysOverdue, formatDate(returnDate));
    }

    private String buildLateNoticeBodyForTeacher(User borrower, String bookTitle, int daysOverdue, LocalDate returnDate) {
        return String.format(
                "Beste %s,\n" +
                "Ons systeem geeft aan dat het boek “%s” sinds %d dag(en) te laat is. " +
                "Uiterste datum: %s.\n" +
                "Gelieve het boek zo snel mogelijk terug te brengen naar de bibliotheek.\n" +
                "Met vriendelijke groeten,\n" +
                "De bibliotheek GoSmartLib",
                displayName(borrower), bookTitle, daysOverdue, formatDate(returnDate));
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "onbekend";
    }

    private String displayName(User user) {
        if (user == null) return "Onbekend";
        return user.getFirstName() != null
                ? user.getFirstName() + " " + user.getName()
                : user.getName();
    }
}