package com.minierp.infrastructure.web.support;

/**
 * Pagination details reported in {@link ApiMeta} for list endpoints.
 */
public record PageMeta(int page, int size, long totalElements, int totalPages) {
}
