package edu.ap.spring_backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.util.text.AES256TextEncryptor;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static AES256TextEncryptor encryptor;

    private static AES256TextEncryptor getEncryptor() {
        if (encryptor == null) {
            String secret = System.getenv("ENCRYPTION_SECRET");
            if (secret == null || secret.isBlank()) {
                secret = System.getProperty("app.encryption.secret");
            }
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("ENCRYPTION_SECRET is not set");
            }
            encryptor = new AES256TextEncryptor();
            encryptor.setPassword(secret);
        }
        return encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return getEncryptor().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return getEncryptor().decrypt(dbData);
        } catch (Exception e) {
            return dbData;
        }
    }
}