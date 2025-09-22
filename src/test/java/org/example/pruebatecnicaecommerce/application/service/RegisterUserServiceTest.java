package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.AuthResponse;
import org.example.pruebatecnicaecommerce.application.dto.RegisterRequest;
import org.example.pruebatecnicaecommerce.domain.model.user.User;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRepository;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRole;
import org.example.pruebatecnicaecommerce.shared.error.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Register User Service Unit Tests")
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private RegisterUserService registerUserService;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "password123");
        String encodedPassword = "encoded_password_123";
        String accessToken = "jwt_token_123";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        User savedUser = User.restore(
                UUID.randomUUID(),
                "user_123",
                request.getUsername(),
                request.getEmail(),
                encodedPassword,
                UserRole.USER,
                Instant.now(),
                0);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser.getUsername(), savedUser.getRole().name())).thenReturn(accessToken);
        when(jwtService.getExpirationTime()).thenReturn(expiresAt);

        // When
        AuthResponse response = registerUserService.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(savedUser.getPublicId(), response.getPublicId());
        assertEquals(savedUser.getUsername(), response.getUsername());
        assertEquals(savedUser.getEmail(), response.getEmail());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(expiresAt, response.getExpiresAt());

        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(savedUser.getUsername(), "USER");
        verify(jwtService).getExpirationTime();
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest("existing_user", "new@example.com", "password123");
        User existingUser = User.create("existing_user", "old@example.com", "old_password");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(existingUser));

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> registerUserService.execute(request));

        assertEquals("User already exists: username: existing_user", exception.getMessage());

        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest("new_user", "existing@example.com", "password123");
        User existingUser = User.create("old_user", "existing@example.com", "old_password");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> registerUserService.execute(request));

        assertEquals("User already exists: email: existing@example.com", exception.getMessage());

        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should properly encode password before saving")
    void shouldProperlyEncodePasswordBeforeSaving() {
        // Given
        RegisterRequest request = new RegisterRequest("test_user", "test@example.com", "plaintext_password");
        String encodedPassword = "bcrypt_encoded_password";
        String accessToken = "jwt_token";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        User savedUser = User.restore(
                UUID.randomUUID(),
                "user_456",
                request.getUsername(),
                request.getEmail(),
                encodedPassword,
                UserRole.USER,
                Instant.now(),
                0);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn(accessToken);
        when(jwtService.getExpirationTime()).thenReturn(expiresAt);

        // When
        registerUserService.execute(request);

        // Then
        verify(passwordEncoder).encode("plaintext_password");

        // Verify the saved user has the encoded password
        verify(userRepository).save(argThat(user -> encodedPassword.equals(user.getPasswordHash())));
    }

    @Test
    @DisplayName("Should create user with USER role by default")
    void shouldCreateUserWithUserRoleByDefault() {
        // Given
        RegisterRequest request = new RegisterRequest("test_user", "test@example.com", "password123");
        String encodedPassword = "encoded_password";
        String accessToken = "jwt_token";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        User savedUser = User.create(request.getUsername(), request.getEmail(), encodedPassword);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn(accessToken);
        when(jwtService.getExpirationTime()).thenReturn(expiresAt);

        // When
        registerUserService.execute(request);

        // Then
        verify(jwtService).generateToken(request.getUsername(), "USER");
        verify(userRepository).save(argThat(user -> UserRole.USER.equals(user.getRole())));
    }

    @Test
    @DisplayName("Should generate JWT token for new user")
    void shouldGenerateJwtTokenForNewUser() {
        // Given
        RegisterRequest request = new RegisterRequest("test_user", "test@example.com", "password123");
        String encodedPassword = "encoded_password";
        String expectedToken = "generated_jwt_token";
        Instant expectedExpiration = Instant.now().plusSeconds(3600);

        User savedUser = User.create(request.getUsername(), request.getEmail(), encodedPassword);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser.getUsername(), "USER")).thenReturn(expectedToken);
        when(jwtService.getExpirationTime()).thenReturn(expectedExpiration);

        // When
        AuthResponse response = registerUserService.execute(request);

        // Then
        assertEquals(expectedToken, response.getAccessToken());
        assertEquals(expectedExpiration, response.getExpiresAt());

        verify(jwtService).generateToken(savedUser.getUsername(), "USER");
        verify(jwtService).getExpirationTime();
    }
}