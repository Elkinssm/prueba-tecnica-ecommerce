package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.constraints.NotBlank;

@Value
public class UserResponse {

    @NotBlank
    String publicId;

    @NotBlank
    String username;

    @NotBlank
    String email;

    @NotBlank
    String role;
}