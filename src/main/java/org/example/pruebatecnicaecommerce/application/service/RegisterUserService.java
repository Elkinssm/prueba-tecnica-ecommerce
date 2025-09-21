package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.AuthResponse;
import org.example.pruebatecnicaecommerce.application.dto.AuthResponseMapper;
import org.example.pruebatecnicaecommerce.application.dto.RegisterRequest;
import org.example.pruebatecnicaecommerce.domain.model.user.User;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRepository;
import org.example.pruebatecnicaecommerce.shared.error.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse execute(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("username: " + request.getUsername());
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("email: " + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(request.getUsername(), request.getEmail(), encodedPassword);

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser.getUsername(), savedUser.getRole().name());
        Instant expiresAt = jwtService.getExpirationTime();

        return AuthResponseMapper.fromDomain(savedUser, accessToken, expiresAt);
    }
}