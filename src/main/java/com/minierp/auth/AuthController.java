package com.minierp.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.auth.dto.LoginRequest;
import com.minierp.auth.dto.LoginResponse;
import com.minierp.auth.dto.MeResponse;
import com.minierp.common.AuthenticatedUser;
import com.minierp.common.CurrentUser;

import jakarta.validation.Valid;

/**
 * Authentication adapter.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me() {
        AuthenticatedUser user = currentUser.get();
        return new MeResponse(user.username(), user.role().name());
    }
}
