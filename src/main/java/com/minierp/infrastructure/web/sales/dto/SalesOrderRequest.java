package com.minierp.infrastructure.web.sales.dto;

import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.port.in.SalesOrderCommand;
import com.minierp.domain.port.in.SalesOrderLineCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a Sales Order.
 */
public record SalesOrderRequest(
        @NotNull(message = "customerId wajib diisi") Long customerId,
        @NotNull(message = "orderDate wajib diisi") LocalDate orderDate,
        String notes,
        @NotEmpty(message = "lines tidak boleh kosong") List<@Valid SalesOrderLineRequest> lines) {

    public SalesOrderCommand toCommand() {
        List<SalesOrderLineCommand> lineCommands = lines == null ? List.of() : lines.stream()
                .map(line -> new SalesOrderLineCommand(line.itemId(), line.qty(), line.unitPrice()))
                .toList();
        return new SalesOrderCommand(customerId, orderDate, notes, lineCommands);
    }
}
