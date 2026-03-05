package edu.ap.spring_backend.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException() {
        super("Entity not found");
    }
}
