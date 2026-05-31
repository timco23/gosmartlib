package edu.ap.spring_backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedStringConverterTest {

    private EncryptedStringConverter converter;

    @BeforeEach
    void setUp() throws Exception {
        converter = new EncryptedStringConverter();
        Field secretField = EncryptedStringConverter.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(converter, "test-secret-for-converter-tests");
        converter.init();
    }

    @Test
    void convertToDatabaseColumn_encryptsValue() {
        String result = converter.convertToDatabaseColumn("Jan");
        assertNotNull(result);
        assertNotEquals("Jan", result);
    }

    @Test
    void convertToEntityAttribute_decryptsValue() {
        String encrypted = converter.convertToDatabaseColumn("Jan");
        String result = converter.convertToEntityAttribute(encrypted);
        assertEquals("Jan", result);
    }

    @Test
    void convertToDatabaseColumn_nullReturnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_nullReturnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_plaintextFallback() {
        String result = converter.convertToEntityAttribute("plaintext");
        assertEquals("plaintext", result);
    }

    @Test
    void encryptDecrypt_roundtrip() {
        String original = "De Meyer";
        String encrypted = converter.convertToDatabaseColumn(original);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encryptSameValueTwice_producesDifferentCiphertext() {
        String enc1 = converter.convertToDatabaseColumn("Jan");
        String enc2 = converter.convertToDatabaseColumn("Jan");
        assertNotEquals(enc1, enc2);
    }
}