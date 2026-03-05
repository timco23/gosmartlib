package edu.ap.spring_backend.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException() {
        super("Entity already exists");
    }
}
