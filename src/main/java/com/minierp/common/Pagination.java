package com.minierp.common;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Normalizes web {@link Pageable} input for list use cases: applies the
 * endpoint's default sort and rejects sort properties outside the allowlist.
 */
public final class Pagination {

    private Pagination() {
    }

    public static Pageable normalize(Pageable requested, Set<String> allowedSortProperties, Sort defaultSort) {
        Sort sort = requested.getSort();
        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                if (!allowedSortProperties.contains(order.getProperty())) {
                    throw new InvalidSortException(order.getProperty(), allowedSortProperties);
                }
            }
        } else {
            sort = defaultSort;
        }
        int page = requested.isPaged() ? requested.getPageNumber() : PaginationDefaults.DEFAULT_PAGE;
        int size = requested.isPaged() ? requested.getPageSize() : PaginationDefaults.DEFAULT_SIZE;
        return PageRequest.of(page, size, sort);
    }
}
