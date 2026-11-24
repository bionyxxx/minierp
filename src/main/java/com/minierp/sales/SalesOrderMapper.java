package com.minierp.sales;

import java.util.List;

import com.minierp.sales.dto.SalesOrderLineResponse;
import com.minierp.sales.dto.SalesOrderResponse;

/**
 * Maps {@link SalesOrder} entities to response DTOs.
 */
final class SalesOrderMapper {

    private SalesOrderMapper() {
    }

    static SalesOrderResponse toResponse(SalesOrder so) {
        List<SalesOrderLineResponse> lines = so.getLines().stream()
                .map(line -> new SalesOrderLineResponse(
                        line.getItem().getId(),
                        line.getItem().getSku(),
                        line.getItem().getName(),
                        line.getQty(),
                        line.getUnitPrice(),
                        line.getLineTotal()))
                .toList();
        return new SalesOrderResponse(
                so.getId(),
                so.getSoNumber(),
                so.getCustomer().getId(),
                so.getCustomer().getName(),
                so.getOrderDate(),
                so.getStatus(),
                so.getTotalAmount(),
                so.getNotes(),
                so.getCreatedBy(),
                so.getCreatedAt(),
                so.getApprovedBy(),
                so.getApprovedAt(),
                so.getRejectReason(),
                lines);
    }
}
