package edu.ap.spring_backend.exception;

public class EmptyReadingListException extends RuntimeException {
    public EmptyReadingListException() {
        super("Cannot save a reading list with no items.");
    }
}