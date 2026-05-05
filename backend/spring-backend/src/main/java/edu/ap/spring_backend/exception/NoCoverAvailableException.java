package edu.ap.spring_backend.exception;

public class NoCoverAvailableException extends RuntimeException {
    public NoCoverAvailableException(Long itemId) {
        super("No cover sources available for item: " + itemId);
    }
}