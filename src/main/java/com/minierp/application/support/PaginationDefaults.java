package com.minierp.application.support;

/**
 * Pagination bounds shared by the web layer and the list use cases.
 */
public final class PaginationDefaults {

    /** Zero-based default page index. */
    public static final int DEFAULT_PAGE = 0;

    /** Default page size when the client sends none. */
    public static final int DEFAULT_SIZE = 20;

    /** Hard cap: larger client requests are clamped to this size. */
    public static final int MAX_SIZE = 100;

    private PaginationDefaults() {
    }
}
