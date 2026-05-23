package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeletionScheduler {

    private final UserRepository userRepository;

    public DeletionScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 3600000) // every hour
    public void deleteExpiredAccounts() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusHours(24);
        List<User> toDelete = userRepository.findByDeletionRequestedAtBefore(cutoff);
        for (User user : toDelete) {
            userRepository.deleteUserByIdNative(user.getId());
        }
    }
}