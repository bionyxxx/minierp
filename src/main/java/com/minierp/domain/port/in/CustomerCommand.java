package com.minierp.domain.port.in;

/**
 * Customer data for create/update use cases.
 */
public record CustomerCommand(String code, String name, String address, String phone, String email) {
}
