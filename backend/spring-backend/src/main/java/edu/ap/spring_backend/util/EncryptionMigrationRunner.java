package edu.ap.spring_backend.util;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Component
public class EncryptionMigrationRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    public EncryptionMigrationRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        File flag = new File("/tmp/encryption_migration_done");
        if (flag.exists()) return;

        EncryptedStringConverter converter = new EncryptedStringConverter();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String encryptedName = user.getName() != null 
                ? converter.convertToDatabaseColumn(user.getName()) 
                : null;
            String encryptedFirstName = user.getFirstName() != null 
                ? converter.convertToDatabaseColumn(user.getFirstName()) 
                : null;
            userRepository.updateNamesDirect(user.getId(), encryptedName, encryptedFirstName);
        }

        flag.createNewFile();
        System.out.println("Encryption migration done for " + users.size() + " users.");
    }
}