package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.constraints.NotBlank;

@Value
public class LoginRequest {

    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "Password is required")
    String password;
}