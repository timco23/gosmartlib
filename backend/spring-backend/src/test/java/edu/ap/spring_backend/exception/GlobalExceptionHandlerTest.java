package edu.ap.spring_backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad", response.getBody());
    }

    @Test
    void handleConstraintViolation_returnsBadRequest() {
        var response = handler.handleConstraintViolation(
                new ConstraintViolationException("cv", Set.of())
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("cv", response.getBody());
    }

    @Test
    void handleIllegalState_returnsConflict() {
        var response = handler.handleIllegalState(new IllegalStateException("state"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("state", response.getBody());
    }

    @Test
    void handleSecurity_returnsForbidden() {
        var response = handler.handleSecurity(new SecurityException("nope"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("nope", response.getBody());
    }

    @Test
    void handleNotFound_returns404() {
        var response = handler.handleNotFound(new EntityNotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Entity not found", response.getBody());
    }

    @Test
    void handleAlreadyExists_returnsConflict() {
        var response = handler.handleAlreadyExists(new EntityAlreadyExistsException());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}