package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import edu.ap.spring_backend.jpa.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sends a Smartschool notification to users who have a book on their wishlist
 * when a copy of that book becomes available (status changes to BESCHIKBAAR).
 * Called from SchoolItemCopyService.updateCopyStatus() when status → BESCHIKBAAR.
 */
@Service
public class WishlistNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WishlistNotificationService.class);

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final SchoolItemCopyRepository copyRepository;
    private final SmartschoolNotificationService notificationService;

    public WishlistNotificationService(UserRepository userRepository,
                                       ItemRepository itemRepository,
                                       SchoolItemCopyRepository copyRepository,
                                       SmartschoolNotificationService notificationService) {
        this.userRepository      = userRepository;
        this.itemRepository      = itemRepository;
        this.copyRepository      = copyRepository;
        this.notificationService = notificationService;
    }

    public void notifyWishlistersIfAvailable(SchoolItemCopy copy) {
        if (copy.getCopy_status() != CopyStatus.BESCHIKBAAR) return;
        if (copy.getItem() == null || copy.getSchool() == null) return;

        Item item = copy.getItem();
        Long schoolId = copy.getSchool().getId();

        // Find users at this school who have this book on their wishlist
        List<User> wishlisted = userRepository.findBySchool_IdAndWishlistedBooks_Id(schoolId, item.getId());

        for (User user : wishlisted) {
            String title = "Boek beschikbaar";
            String desc  = String.format("'%s' is nu beschikbaar in de bibliotheek.", item.getTitle());
            String msgBody = String.format(
                    "Beste %s,<br><br>" +
                    "Goed nieuws! Het boek <strong>%s</strong> van %s staat nu in uw verlanglijst " +
                    "en is nu beschikbaar in de bibliotheek.<br><br>" +
                    "Bezoek snel de bibliotheek om het uit te lenen, want het is op = op!<br><br>" +
                    "Met vriendelijke groeten,<br>De bibliotheek",
                    displayName(user),
                    item.getTitle(),
                    item.getAuthor() != null ? item.getAuthor() : "onbekende auteur");

            notificationService.sendNotificationAndMessage(
                    user,
                    title, desc, "/books/" + item.getId(),
                    title, msgBody);

            log.debug("Wishlist notification sent to {} for book '{}'", user.getEmail(), item.getTitle());
        }
    }

    private String displayName(User user) {
        if (user == null) return "Gebruiker";
        return user.getFirstName() != null
                ? user.getFirstName() + " " + user.getName()
                : user.getName();
    }
}