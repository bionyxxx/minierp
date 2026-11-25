package com.minierp.infrastructure.web.sales.dto;

import java.math.BigDecimal;

import com.minierp.domain.model.sales.SalesOrderLine;

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

    public static SalesOrderLineResponse from(SalesOrderLine line) {
        return new SalesOrderLineResponse(line.getItem().getId(), line.getItem().getSku(),
                line.getItem().getName(), line.getQty(), line.getUnitPrice(), line.getLineTotal());
    }
}
