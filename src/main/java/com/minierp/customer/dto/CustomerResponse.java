package com.minierp.customer.dto;

import com.minierp.customer.Customer;

/**
 * External contract of a customer.
 */
public record CustomerResponse(Long id, String code, String name, String address,
        String phone, String email, boolean active) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getCode(), customer.getName(),
                customer.getAddress(), customer.getPhone(), customer.getEmail(), customer.isActive());
    }
}
