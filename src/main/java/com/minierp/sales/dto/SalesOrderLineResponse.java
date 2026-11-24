package com.minierp.sales.dto;

import java.math.BigDecimal;

/**
 * One line of a Sales Order response.
 */
public record SalesOrderLineResponse(
        Long itemId,
        String sku,
        String itemName,
        BigDecimal qty,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
