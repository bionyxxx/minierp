package com.minierp.infrastructure.web.sales.dto;

import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.port.in.DeliveryCommand;
import com.minierp.domain.port.in.DeliveryLineCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a Delivery against an approved Sales Order.
 */
public record DeliveryRequest(
        @NotNull(message = "salesOrderId wajib diisi") Long salesOrderId,
        @NotNull(message = "warehouseId wajib diisi") Long warehouseId,
        @NotNull(message = "deliveryDate wajib diisi") LocalDate deliveryDate,
        String notes,
        @NotEmpty(message = "lines tidak boleh kosong") List<@Valid DeliveryLineRequest> lines) {

    public DeliveryCommand toCommand() {
        List<DeliveryLineCommand> lineCommands = lines == null ? List.of() : lines.stream()
                .map(line -> new DeliveryLineCommand(line.itemId(), line.qty()))
                .toList();
        return new DeliveryCommand(salesOrderId, warehouseId, deliveryDate, notes, lineCommands);
    }
}
