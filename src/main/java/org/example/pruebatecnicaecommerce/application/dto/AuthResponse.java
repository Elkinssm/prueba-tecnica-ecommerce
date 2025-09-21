package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Value
public class AuthResponse {

    @NotBlank
    String publicId;

    @NotBlank
    String username;

    @NotBlank
    String email;

    @NotBlank
    String role;

    @NotBlank
    String accessToken;

    Instant expiresAt;
}