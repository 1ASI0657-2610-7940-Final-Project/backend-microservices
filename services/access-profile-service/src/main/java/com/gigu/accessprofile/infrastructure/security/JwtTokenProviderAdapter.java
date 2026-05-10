package com.gigu.accessprofile.infrastructure.security;

import com.gigu.accessprofile.application.port.out.TokenProviderPort;
import com.gigu.accessprofile.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {
    private final byte[] secret;
    public JwtTokenProviderAdapter(@Value("${JWT_SECRET:jwt-secret-change-me-jwt-secret-change-me}") String secret) { this.secret = secret.getBytes(StandardCharsets.UTF_8); }

    @Override
    public String generate(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.id().toString()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600)))
                .claim("email", user.email())
                .claim("roles", user.roles().stream().map(Enum::name).collect(Collectors.toList()))
                .signWith(Keys.hmacShaKeyFor(secret)).compact();
    }
    @Override
    public String extractSubject(String token) { return claims(token).getSubject(); }
    @Override
    public boolean isValid(String token) { try { claims(token); return true; } catch (Exception e) { return false; } }
    private Claims claims(String token) { return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret)).build().parseSignedClaims(token).getPayload(); }
}
