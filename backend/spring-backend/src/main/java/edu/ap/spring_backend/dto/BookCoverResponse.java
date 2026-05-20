package edu.ap.spring_backend.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Response wrapper for a book cover request.
 * Contains image data, metadata, and cache status.
 */
public record BookCoverResponse(byte[] bytes, String contentType, String etag, boolean notModified) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookCoverResponse(var bytes, var contentType, var etag, var notModified))) return false;
        return notModified == notModified()
                && Arrays.equals(bytes(), bytes)
                && Objects.equals(contentType(), contentType)
                && Objects.equals(etag(), etag);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(contentType, etag, notModified);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public String toString() {
        return "BookCoverResponse[bytes=" + Arrays.toString(bytes)
                + ", contentType=" + contentType
                + ", etag=" + etag
                + ", notModified=" + notModified + "]";
    }
}
