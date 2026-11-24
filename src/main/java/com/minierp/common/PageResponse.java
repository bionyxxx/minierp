package com.minierp.common;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Stable pagination envelope for list endpoints. Kept independent of Spring
 * Data's {@link Page} serialization so the JSON contract cannot drift.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public PageResponse {
        content = List.copyOf(content);
    }

    public static <T> PageResponse<T> from(Page<T> source) {
        return new PageResponse<>(source.getContent(), source.getNumber(), source.getSize(),
                source.getTotalElements(), source.getTotalPages());
    }
}
