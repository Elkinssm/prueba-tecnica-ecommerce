package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.AuthResponse;
import org.example.pruebatecnicaecommerce.application.dto.LoginRequest;
import org.example.pruebatecnicaecommerce.domain.model.user.User;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRepository;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRole;
import org.example.pruebatecnicaecommerce.shared.error.AuthenticationException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Login User Service Unit Tests")
class LoginUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private LoginUserService loginUserService;

    @BeforeEach
    void setUp() {
        loginUserService = new LoginUserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Should login user successfully with correct credentials")
    void shouldLoginUserSuccessfully() {
        // Given
        String username = "john_doe";
        String password = "correct_password";
        LoginRequest request = new LoginRequest(username, password);

        String accessToken = "jwt_token_123";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        User existingUser = User.restore(
                UUID.randomUUID(),
                "user_123",
                username,
                "john@example.com",
                password, // Note: In the actual implementation, this seems to compare plain text password
                UserRole.USER,
                Instant.now(),
                0);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser.getUsername(), existingUser.getRole().name()))
                .thenReturn(accessToken);
        when(jwtService.getExpirationTime()).thenReturn(expiresAt);

        // When
        AuthResponse response = loginUserService.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(existingUser.getPublicId(), response.getPublicId());
        assertEquals(existingUser.getUsername(), response.getUsername());
        assertEquals(existingUser.getEmail(), response.getEmail());
        assertEquals("USER", response.getRole());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(expiresAt, response.getExpiresAt());

        verify(userRepository).findByUsername(username);
        verify(jwtService).generateToken(existingUser.getUsername(), "USER");
        verify(jwtService).getExpirationTime();
    }

    @Test
    @DisplayName("Should throw exception when username does not exist")
    void shouldThrowExceptionWhenUsernameDoesNotExist() {
        // Given
        String username = "nonexistent_user";
        String password = "any_password";
        LoginRequest request = new LoginRequest(username, password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> loginUserService.execute(request));

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository).findByUsername(username);
        verify(jwtService, never()).generateToken(anyString(), anyString());
        verify(jwtService, never()).getExpirationTime();
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        String username = "john_doe";
        String correctPassword = "correct_password";
        String wrongPassword = "wrong_password";
        LoginRequest request = new LoginRequest(username, wrongPassword);

        User existingUser = User.restore(
                UUID.randomUUID(),
                "user_123",
                username,
                "john@example.com",
                correctPassword,
                UserRole.USER,
                Instant.now(),
                0);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // When & Then
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> loginUserService.execute(request));

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository).findByUsername(username);
        verify(jwtService, never()).generateToken(anyString(), anyString());
        verify(jwtService, never()).getExpirationTime();
    }

    @Test
    @DisplayName("Should login admin user successfully")
    void shouldLoginAdminUserSuccessfully() {
        // Given
        String username = "admin";
        String password = "admin_password";
        LoginRequest request = new LoginRequest(username, password);

        String accessToken = "admin_jwt_token";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        User adminUser = User.restore(
                UUID.randomUUID(),
                "admin_123",
                username,
                "admin@example.com",
                password,
                UserRole.ADMIN,
                Instant.now(),
                0);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));
        when(jwtService.generateToken(adminUser.getUsername(), adminUser.getRole().name())).thenReturn(accessToken);
        when(jwtService.getExpirationTime()).thenReturn(expiresAt);

        // When
        AuthResponse response = loginUserService.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(adminUser.getPublicId(), response.getPublicId());
        assertEquals(adminUser.getUsername(), response.getUsername());
        assertEquals(adminUser.getEmail(), response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(expiresAt, response.getExpiresAt());

        verify(userRepository).findByUsername(username);
        verify(jwtService).generateToken(adminUser.getUsername(), "ADMIN");
        verify(jwtService).getExpirationTime();
    }

    @Test
    @DisplayName("Should generate correct JWT token for logged in user")
    void shouldGenerateCorrectJwtTokenForLoggedInUser() {
        // Given
        String username = "test_user";
        String password = "test_password";
        LoginRequest request = new LoginRequest(username, password);

        String expectedToken = "expected_jwt_token";
        Instant expectedExpiration = Instant.now().plusSeconds(3600);

        User user = User.restore(
                UUID.randomUUID(),
                "user_456",
                username,
                "test@example.com",
                password,
                UserRole.USER,
                Instant.now(),
                0);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user.getUsername(), "USER")).thenReturn(expectedToken);
        when(jwtService.getExpirationTime()).thenReturn(expectedExpiration);

        // When
        AuthResponse response = loginUserService.execute(request);

        // Then
        assertEquals(expectedToken, response.getAccessToken());
        assertEquals(expectedExpiration, response.getExpiresAt());

        verify(jwtService).generateToken(user.getUsername(), "USER");
        verify(jwtService).getExpirationTime();
    }

    @Test
    @DisplayName("Should handle empty password correctly")
    void shouldHandleEmptyPasswordCorrectly() {
        // Given
        String username = "test_user";
        String userPassword = "stored_password";
        String emptyPassword = "";
        LoginRequest request = new LoginRequest(username, emptyPassword);

        User user = User.restore(
                UUID.randomUUID(),
                "user_789",
                username,
                "test@example.com",
                userPassword,
                UserRole.USER,
                Instant.now(),
                0);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When & Then
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> loginUserService.execute(request));

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository).findByUsername(username);
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }
}