package edu.ap.spring_backend.util;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

@Component
public class EncryptionMigrationRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final EncryptedStringConverter converter;
    private File flagFile = Paths.get(System.getProperty("java.io.tmpdir"), "encryption_migration_done").toFile();

    public EncryptionMigrationRunner(UserRepository userRepository, EncryptedStringConverter converter) {
        this.userRepository = userRepository;
        this.converter = converter;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (flagFile.exists()) return;

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

        flagFile.createNewFile();
        System.out.println("Encryption migration done for " + users.size() + " users.");
    }
}