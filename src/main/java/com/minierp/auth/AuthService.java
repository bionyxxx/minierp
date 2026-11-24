package com.minierp.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.auth.dto.LoginRequest;
import com.minierp.auth.dto.LoginResponse;
import com.minierp.user.User;
import com.minierp.user.UserRepository;

/**
 * Login use case: validates credentials and issues a JWT.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        }
        catch (AuthenticationException ex) {
            throw new BadCredentialsException("Username atau password salah");
        }
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Username atau password salah"));
        JwtService.IssuedToken issued = jwtService.issue(user);
        return new LoginResponse(issued.token(), issued.tokenType(), issued.expiresAt(),
                user.getUsername(), user.getFullName(), user.getRole().name());
    }
}
