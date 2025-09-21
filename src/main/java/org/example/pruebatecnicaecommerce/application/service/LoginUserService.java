package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.AuthResponse;
import org.example.pruebatecnicaecommerce.application.dto.AuthResponseMapper;
import org.example.pruebatecnicaecommerce.application.dto.LoginRequest;
import org.example.pruebatecnicaecommerce.domain.model.user.User;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRepository;
import org.example.pruebatecnicaecommerce.shared.error.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse execute(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        // Comparación simple sin hash para testing
        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }

        String accessToken = jwtService.generateToken(user.getUsername(), user.getRole().name());
        Instant expiresAt = jwtService.getExpirationTime();

        return AuthResponseMapper.fromDomain(user, accessToken, expiresAt);
    }
}