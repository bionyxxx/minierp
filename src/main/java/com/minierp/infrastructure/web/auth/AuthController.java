package com.minierp.infrastructure.web.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.port.in.AuthUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.auth.dto.LoginRequest;
import com.minierp.infrastructure.web.auth.dto.LoginResponse;
import com.minierp.infrastructure.web.auth.dto.MeResponse;

import jakarta.validation.Valid;

/**
 * Authentication adapter.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final CurrentUser currentUser;

    public AuthController(AuthUseCase authUseCase, CurrentUser currentUser) {
        this.authUseCase = authUseCase;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(authUseCase.login(request.toCommand()));
    }

    @GetMapping("/me")
    public MeResponse me() {
        AuthenticatedUser user = currentUser.get();
        return new MeResponse(user.username(), user.role().name());
    }
}
