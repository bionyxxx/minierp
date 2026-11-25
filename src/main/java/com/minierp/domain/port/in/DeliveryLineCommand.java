package com.minierp.domain.port.in;

import java.math.BigDecimal;

/**
 * One line of a create-Delivery command.
 */
public record DeliveryLineCommand(Long itemId, BigDecimal qty) {
}
