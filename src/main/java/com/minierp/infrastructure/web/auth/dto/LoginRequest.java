package com.minierp.infrastructure.web.auth.dto;

import com.minierp.domain.port.in.LoginCommand;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username wajib diisi") String username,
        @NotBlank(message = "password wajib diisi") String password) {

    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
