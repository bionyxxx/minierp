package com.minierp.application.support;

import com.minierp.domain.model.shared.InvalidSortException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.SortField;

class PaginationTest {

    private static final Set<String> ALLOWED = Set.of("sku", "name");

    @Test
    void normalizeQueryAppliesMultiFieldDefaultSortWhenNoneRequested() {
        List<SortField> defaultSort = List.of(SortField.asc("item.sku"), SortField.asc("warehouse.code"));
        PageQuery result = Pagination.normalize(new PageQuery(null, null, List.of()), ALLOWED, defaultSort);

        assertEquals(defaultSort, result.sort());
        assertEquals(PaginationDefaults.DEFAULT_PAGE, result.page());
        assertEquals(PaginationDefaults.DEFAULT_SIZE, result.size());
    }

    @Test
    void normalizeQueryKeepsAllowedCustomSortOverMultiFieldDefault() {
        List<SortField> defaultSort = List.of(SortField.desc("createdAt"), SortField.desc("id"));
        PageQuery result = Pagination.normalize(new PageQuery(0, 20, List.of(SortField.desc("name"))), ALLOWED,
                defaultSort);

        assertEquals(List.of(SortField.desc("name")), result.sort());
    }

    @Test
    void normalizeQueryAppliesDefaultSortWhenNoneRequested() {
        PageQuery result = Pagination.normalize(new PageQuery(1, 5, List.of()), ALLOWED, SortField.asc("sku"));

        assertEquals(List.of(SortField.asc("sku")), result.sort());
        assertEquals(1, result.page());
        assertEquals(5, result.size());
    }

    @Test
    void normalizeQueryKeepsAllowedCustomSort() {
        PageQuery result = Pagination.normalize(new PageQuery(0, 20, List.of(SortField.desc("name"))), ALLOWED,
                SortField.asc("sku"));

        assertEquals(List.of(SortField.desc("name")), result.sort());
    }

    @Test
    void normalizeQueryRejectsUnknownSortProperty() {
        InvalidSortException exception = assertThrows(InvalidSortException.class,
                () -> Pagination.normalize(new PageQuery(0, 20, List.of(SortField.asc("hacker"))), ALLOWED,
                        SortField.asc("sku")));

        assertEquals("hacker", exception.getProperty());
        assertEquals(ALLOWED, exception.getAllowedProperties());
    }

    @Test
    void normalizeQueryFallsBackToDefaultsForUnpagedRequests() {
        PageQuery result = Pagination.normalize(new PageQuery(null, null, List.of()), ALLOWED, SortField.asc("sku"));

        assertEquals(PaginationDefaults.DEFAULT_PAGE, result.page());
        assertEquals(PaginationDefaults.DEFAULT_SIZE, result.size());
    }

    @Test
    void toQueryAndBackToPageableRoundTrips() {
        PageQuery query = Pagination.toQuery(PageRequest.of(2, 30, Sort.by(Sort.Order.desc("name"))));
        Pageable pageable = Pagination.toPageable(query);

        assertEquals(PageRequest.of(2, 30, Sort.by(Sort.Order.desc("name"))), pageable);
    }
}
