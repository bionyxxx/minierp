package com.minierp.domain.port.in;

import java.math.BigDecimal;

/**
 * Item data for create/update use cases.
 */
public record ItemCommand(String sku, String name, String uom,
        BigDecimal minStock, BigDecimal purchasePrice, BigDecimal sellingPrice) {
}
