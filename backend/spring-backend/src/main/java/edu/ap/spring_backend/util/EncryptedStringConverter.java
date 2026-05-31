package edu.ap.spring_backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final AES256TextEncryptor encryptor = new AES256TextEncryptor();

    @Value("${ENCRYPTION_SECRET:}")
    private String secret;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            // Fallback for non-Spring managed instantiation if necessary
            secret = System.getenv("ENCRYPTION_SECRET");
            if (secret == null || secret.isBlank()) {
                secret = System.getProperty("ENCRYPTION_SECRET");
            }
        }

        if (secret != null && !secret.isBlank()) {
            encryptor.setPassword(secret);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || secret == null || secret.isBlank()) return attribute;
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || secret == null || secret.isBlank()) return dbData;
        try {
            return encryptor.decrypt(dbData);
        } catch (Exception e) {
            return dbData;
        }
    }
}