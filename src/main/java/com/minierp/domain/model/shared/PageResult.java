package com.minierp.domain.model.shared;

import java.util.List;
import java.util.function.Function;

/**
 * Framework-free page of results returned by list use cases and driven ports.
 * The web layer converts it to the stable {@code PageResponse} envelope.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public PageResult {
        content = List.copyOf(content);
    }

    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = content.stream().<R>map(mapper).toList();
        return new PageResult<>(mapped, page, size, totalElements, totalPages);
    }
}
