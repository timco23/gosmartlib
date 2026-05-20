package edu.ap.spring_backend.exception;

public class EmptyCollectionException extends RuntimeException {
    public EmptyCollectionException() {
        super("Cannot save a collection with no items.");
    }
}