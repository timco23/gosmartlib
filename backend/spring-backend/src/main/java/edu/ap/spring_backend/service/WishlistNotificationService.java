package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import edu.ap.spring_backend.jpa.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sends a Smartschool notification to users who have a book on their wishlist
 * when a copy of that book becomes available (status changes to BESCHIKBAAR).
 * Called from SchoolItemCopyService.updateCopyStatus() when status → BESCHIKBAAR.
 */
@Service
public class WishlistNotificationService {

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
            String title = "Boek beschikbaar op uw verlanglijst";
            String desc  = "Het boek \"" + item.getTitle() + "\" is nu beschikbaar.";
            String msgBody = String.format(
                    "Beste %s, het boek \"%s\" staat nu op uw verlanglijst en is beschikbaar in de bibliotheek. " +
                    "Bezoek snel de bibliotheek om het uit te lenen. Met vriendelijke groeten, De bibliotheek GoSmartLib",
                    displayName(user),
                    item.getTitle());

            notificationService.sendNotificationAndMessage(
                    user,
                    title, desc, null,
                    title, msgBody);
        }
    }

    private String displayName(User user) {
        if (user == null) return "Gebruiker";
        return user.getFirstName() != null
                ? user.getFirstName() + " " + user.getName()
                : user.getName();
    }
}