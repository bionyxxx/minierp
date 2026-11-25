package com.minierp.domain.model.shared;

/**
 * One sort criterion of a list query.
 */
public record SortField(String property, boolean ascending) {

    public static SortField asc(String property) {
        return new SortField(property, true);
    }

    public static SortField desc(String property) {
        return new SortField(property, false);
    }
}
