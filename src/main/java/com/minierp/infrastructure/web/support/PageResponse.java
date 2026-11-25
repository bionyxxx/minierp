package com.minierp.infrastructure.web.support;

import java.util.List;

import com.minierp.domain.model.shared.PageResult;

/**
 * Stable pagination envelope for list endpoints. Kept independent of Spring
 * Data's {@code Page} serialization so the JSON contract cannot drift.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public PageResponse {
        content = List.copyOf(content);
    }

    public static <T> PageResponse<T> from(PageResult<T> source) {
        return new PageResponse<>(source.content(), source.page(), source.size(),
                source.totalElements(), source.totalPages());
    }
}
