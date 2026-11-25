package com.minierp.domain.model.shared;

import java.util.List;

/**
 * Framework-free list query input. {@code page} and {@code size} are
 * {@code null} for an unpaged request and an empty {@code sort} means the
 * caller has not requested a specific order; the use case normalizes both
 * against its own defaults (see {@code Pagination}).
 */
public record PageQuery(Integer page, Integer size, List<SortField> sort) {

    public PageQuery {
        sort = List.copyOf(sort);
    }
}
