package com.minierp.item.dto;

import java.math.BigDecimal;

import com.minierp.item.Item;

/**
 * External contract of an item.
 */
public record ItemResponse(Long id, String sku, String name, String uom,
        BigDecimal minStock, BigDecimal purchasePrice, BigDecimal sellingPrice, boolean active) {

    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getSku(), item.getName(), item.getUom(),
                item.getMinStock(), item.getPurchasePrice(), item.getSellingPrice(), item.isActive());
    }
}
