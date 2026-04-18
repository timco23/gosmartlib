package edu.ap.spring_backend.dto;

/**
 * Response wrapper for a book cover request.
 * Contains image data, metadata, and cache status.
 */
public record BookCoverResponse(byte[] bytes, String contentType, String etag, boolean notModified) {}
