package com.minierp.infrastructure.web.customer.dto;

import com.minierp.domain.port.in.CustomerCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for a customer.
 */
public record CustomerRequest(
        @NotBlank(message = "kode wajib diisi") String code,
        @NotBlank(message = "nama wajib diisi") String name,
        String address,
        String phone,
        @Email(message = "format email tidak valid") String email) {

    public CustomerCommand toCommand() {
        return new CustomerCommand(code, name, address, phone, email);
    }
}
