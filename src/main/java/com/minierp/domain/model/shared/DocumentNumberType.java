package com.minierp.domain.model.shared;

/**
 * Kinds of numbered business documents. Each kind owns an external number
 * prefix and a dedicated database sequence; allocation happens through the
 * {@code DocumentNumberPort}.
 */
public enum DocumentNumberType {

    PURCHASE_ORDER("PO"),
    GOODS_RECEIPT("GRN"),
    SALES_ORDER("SO"),
    DELIVERY("DEL");

    private final String prefix;

    DocumentNumberType(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }

    /**
     * Formats a pre-allocated sequence value as the external document number,
     * e.g. {@code PO-000123}.
     */
    public String format(long value) {
        return "%s-%06d".formatted(prefix, value);
    }
}
