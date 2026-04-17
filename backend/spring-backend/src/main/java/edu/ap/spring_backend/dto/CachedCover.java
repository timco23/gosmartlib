package edu.ap.spring_backend.dto;

/**
 * Cached cover entry with metadata and expiration.
 */
public record CachedCover(String url, byte[] bytes, String contentType, String etag, long expiresAt) {}
