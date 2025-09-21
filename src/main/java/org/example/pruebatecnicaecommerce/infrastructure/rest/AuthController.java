package org.example.pruebatecnicaecommerce.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.AuthResponse;
import org.example.pruebatecnicaecommerce.application.dto.LoginRequest;
import org.example.pruebatecnicaecommerce.application.dto.RegisterRequest;
import org.example.pruebatecnicaecommerce.application.service.LoginUserService;
import org.example.pruebatecnicaecommerce.application.service.RegisterUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = registerUserService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = loginUserService.execute(request);
        return ResponseEntity.ok(response);
    }
}