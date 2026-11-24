package com.minierp.auth;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.minierp.config.MinierpProperties;
import com.minierp.user.User;

/**
 * Issues signed JWTs for authenticated users.
 */
@Service
public class JwtService {

    private static final String ISSUER = "minierp";

    private final JwtEncoder jwtEncoder;
    private final MinierpProperties properties;

    public JwtService(JwtEncoder jwtEncoder, MinierpProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public IssuedToken issue(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.security().jwt().expiration());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("name", user.getFullName())
                .claim("role", user.getRole().name())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new IssuedToken(token, "Bearer", expiresAt);
    }

    public record IssuedToken(String token, String tokenType, Instant expiresAt) {
    }
}
