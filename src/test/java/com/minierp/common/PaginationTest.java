package com.minierp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaginationTest {

    private static final Set<String> ALLOWED = Set.of("sku", "name");

    @Test
    void normalizeAppliesDefaultSortWhenNoneRequested() {
        Pageable result = Pagination.normalize(PageRequest.of(1, 5), ALLOWED, Sort.by("sku"));

        assertEquals(Sort.by("sku"), result.getSort());
        assertEquals(1, result.getPageNumber());
        assertEquals(5, result.getPageSize());
    }

    @Test
    void normalizeKeepsAllowedCustomSort() {
        Pageable result = Pagination.normalize(PageRequest.of(0, 20, Sort.by(Sort.Order.desc("name"))), ALLOWED,
                Sort.by("sku"));

        assertEquals(Sort.by(Sort.Order.desc("name")), result.getSort());
    }

    @Test
    void normalizeRejectsUnknownSortProperty() {
        InvalidSortException exception = assertThrows(InvalidSortException.class,
                () -> Pagination.normalize(PageRequest.of(0, 20, Sort.by("hacker")), ALLOWED, Sort.by("sku")));

        assertEquals("hacker", exception.getProperty());
        assertEquals(ALLOWED, exception.getAllowedProperties());
    }

    @Test
    void normalizeFallsBackToDefaultsForUnpagedRequests() {
        Pageable result = Pagination.normalize(Pageable.unpaged(), ALLOWED, Sort.by("sku"));

        assertEquals(PaginationDefaults.DEFAULT_PAGE, result.getPageNumber());
        assertEquals(PaginationDefaults.DEFAULT_SIZE, result.getPageSize());
    }
}
