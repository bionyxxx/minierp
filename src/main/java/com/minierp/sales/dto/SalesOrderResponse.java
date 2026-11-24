package com.minierp.sales.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.common.DocumentStatus;

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
}
