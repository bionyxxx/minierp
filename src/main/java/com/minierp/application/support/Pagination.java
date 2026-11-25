package com.minierp.application.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.minierp.domain.model.shared.InvalidSortException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.SortField;

/**
 * Normalizes list query input for the use cases: converts between the web
 * {@link Pageable} and the framework-free {@link PageQuery}, applies the
 * use case's default sort, and rejects sort properties outside the allowlist.
 */
public final class Pagination {

    private Pagination() {
    }

    /**
     * Converts web {@link Pageable} input to the framework-free query model.
     * Unpaged or unsorted input stays empty so {@link #normalize} can apply
     * the use case's defaults.
     */
    public static PageQuery toQuery(Pageable requested) {
        List<SortField> sort = new ArrayList<>();
        for (Sort.Order order : requested.getSort()) {
            sort.add(new SortField(order.getProperty(), order.isAscending()));
        }
        Integer page = requested.isPaged() ? requested.getPageNumber() : null;
        Integer size = requested.isPaged() ? requested.getPageSize() : null;
        return new PageQuery(page, size, sort);
    }

    /**
     * Normalizes a list query: validates every sort property against the
     * use case's allowlist and falls back to the given default sort, page,
     * and size when the request carries none.
     */
    public static PageQuery normalize(PageQuery requested, Set<String> allowedSortProperties, SortField defaultSort) {
        return normalize(requested, allowedSortProperties, List.of(defaultSort));
    }

    /**
     * Normalizes a list query with a multi-field default sort.
     */
    public static PageQuery normalize(PageQuery requested, Set<String> allowedSortProperties,
            List<SortField> defaultSort) {
        List<SortField> sort = requested.sort();
        if (sort.isEmpty()) {
            sort = defaultSort;
        } else {
            for (SortField field : sort) {
                if (!allowedSortProperties.contains(field.property())) {
                    throw new InvalidSortException(field.property(), allowedSortProperties);
                }
            }
        }
        int page = requested.page() != null ? requested.page() : PaginationDefaults.DEFAULT_PAGE;
        int size = requested.size() != null ? requested.size() : PaginationDefaults.DEFAULT_SIZE;
        return new PageQuery(page, size, sort);
    }

    /**
     * Adapts a normalized query to Spring Data for the persistence adapters.
     */
    public static Pageable toPageable(PageQuery query) {
        Sort sort = Sort.by(query.sort().stream()
                .map(field -> field.ascending() ? Sort.Order.asc(field.property()) : Sort.Order.desc(field.property()))
                .toList());
        return PageRequest.of(query.page(), query.size(), sort);
    }
}
