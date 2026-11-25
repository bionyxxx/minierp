package com.minierp.infrastructure.web.sales.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.shared.DocumentStatus;

/**
 * Sales Order response, including its lines.
 */
public record SalesOrderResponse(
        Long id,
        String soNumber,
        Long customerId,
        String customerName,
        LocalDate orderDate,
        DocumentStatus status,
        BigDecimal totalAmount,
        String notes,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt,
        String rejectReason,
        List<SalesOrderLineResponse> lines) {

    public static SalesOrderResponse from(SalesOrder so) {
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
                so.getLines().stream().map(SalesOrderLineResponse::from).toList());
    }
}
