package org.example.pruebatecnicaecommerce.application.dto;

import org.example.pruebatecnicaecommerce.domain.model.user.User;

import java.time.Instant;

public class AuthResponseMapper {

    public static AuthResponse fromDomain(User user, String accessToken, Instant expiresAt) {
        return new AuthResponse(
                user.getPublicId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                expiresAt);
    }
}