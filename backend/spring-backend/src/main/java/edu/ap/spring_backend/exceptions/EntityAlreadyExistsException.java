package edu.ap.spring_backend.exceptions;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException() {
        super("Entity already exists");
    }
}
