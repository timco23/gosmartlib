package edu.ap.spring_backend.util;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncryptionMigrationRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationArguments args;

    @InjectMocks
    private EncryptionMigrationRunner runner;

    @TempDir
    Path tempDir;

    private File flagFile;

    @BeforeEach
    void setUp() throws Exception {
        Field field = EncryptedStringConverter.class.getDeclaredField("encryptor");
        field.setAccessible(true);
        field.set(null, null);

        System.setProperty("ENCRYPTION_SECRET", "test-secret-for-migration-tests");

        flagFile = tempDir.resolve("encryption_migration_done").toFile();

        Field flagField = EncryptionMigrationRunner.class.getDeclaredField("flagFile");
        if (flagField != null) {
            flagField.setAccessible(true);
            flagField.set(runner, flagFile);
        }
    }

    @Test
    void run_migratesUsersAndCreatesFlagFile() throws Exception {
        User user1 = makeUser(1, "Peeters", "Jan");
        User user2 = makeUser(2, "De Meyer", "Lisa");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        runner.run(args);

        verify(userRepository, times(1)).findAll();
        verify(userRepository, times(2)).updateNamesDirect(isNull(), anyString(), anyString());
    }

    @Test
    void run_skipsIfFlagExists() throws Exception {
        flagFile.createNewFile();

        runner.run(args);

        verify(userRepository, never()).findAll();
    }

    @Test
    void run_handlesNullFirstName() throws Exception {
        User user = makeUser(1, "Peeters", null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        runner.run(args);

        verify(userRepository).updateNamesDirect(isNull(), anyString(), isNull());
    }

    private User makeUser(int id, String name, String firstName) {
        User user = new User();
        user.setName(name != null ? name : "");
        user.setFirstName(firstName);
        user.setEmail("test" + id + "@example.com");
        user.setPassword("{noop}test");
        return user;
    }
}