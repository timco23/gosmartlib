package edu.ap.spring_backend.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Cached cover entry with metadata and expiration.
 */
public record CachedCover(String url, byte[] bytes, String contentType, String etag, long expiresAt) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CachedCover(var url, var bytes, var contentType, var etag, var expiresAt))) return false;
        return expiresAt == expiresAt()
                && Arrays.equals(bytes(), bytes)
                && Objects.equals(url(), url)
                && Objects.equals(contentType(), contentType)
                && Objects.equals(etag(), etag);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(url, contentType, etag, expiresAt);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public String toString() {
        return "CachedCover[url=" + url
                + ", bytes=" + Arrays.toString(bytes)
                + ", contentType=" + contentType
                + ", etag=" + etag
                + ", expiresAt=" + expiresAt + "]";
    }
}
