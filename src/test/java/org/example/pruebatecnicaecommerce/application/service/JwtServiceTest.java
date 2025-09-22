package org.example.pruebatecnicaecommerce.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Service Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "myTestSecretKeyThatIsLongEnoughForHMACAlgorithm");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600L);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidJwtToken() {
        // Given
        String username = "test_user";
        String role = "USER";

        // When
        String token = jwtService.generateToken(username, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        String username = "test_user";
        String role = "USER";
        String token = jwtService.generateToken(username, role);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should extract role from token")
    void shouldExtractRoleFromToken() {
        // Given
        String username = "test_user";
        String role = "ADMIN";
        String token = jwtService.generateToken(username, role);

        // When
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Should validate token successfully for correct username")
    void shouldValidateTokenSuccessfully() {
        // Given
        String username = "test_user";
        String role = "USER";
        String token = jwtService.generateToken(username, role);

        // When
        boolean isValid = jwtService.isTokenValid(token, username);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject token for incorrect username")
    void shouldRejectTokenForIncorrectUsername() {
        // Given
        String username = "test_user";
        String wrongUsername = "wrong_user";
        String role = "USER";
        String token = jwtService.generateToken(username, role);

        // When
        boolean isValid = jwtService.isTokenValid(token, wrongUsername);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should return expiration time in the future")
    void shouldReturnExpirationTimeInFuture() {
        // Given
        Instant beforeCall = Instant.now();

        // When
        Instant expirationTime = jwtService.getExpirationTime();

        // Then
        Instant afterCall = Instant.now();
        assertTrue(expirationTime.isAfter(beforeCall.plus(3590, ChronoUnit.SECONDS))); // A bit less than 3600
        assertTrue(expirationTime.isBefore(afterCall.plus(3610, ChronoUnit.SECONDS))); // A bit more than 3600
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        String user1 = "user1";
        String user2 = "user2";
        String role = "USER";

        // When
        String token1 = jwtService.generateToken(user1, role);
        String token2 = jwtService.generateToken(user2, role);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should generate different tokens for different roles")
    void shouldGenerateDifferentTokensForDifferentRoles() {
        // Given
        String username = "test_user";
        String role1 = "USER";
        String role2 = "ADMIN";

        // When
        String token1 = jwtService.generateToken(username, role1);
        String token2 = jwtService.generateToken(username, role2);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should handle token with admin role")
    void shouldHandleTokenWithAdminRole() {
        // Given
        String username = "admin_user";
        String role = "ADMIN";

        // When
        String token = jwtService.generateToken(username, role);
        String extractedUsername = jwtService.extractUsername(token);
        String extractedRole = jwtService.extractRole(token);
        boolean isValid = jwtService.isTokenValid(token, username);

        // Then
        assertEquals(username, extractedUsername);
        assertEquals(role, extractedRole);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should create token with correct structure")
    void shouldCreateTokenWithCorrectStructure() {
        // Given
        String username = "test_user";
        String role = "USER";

        // When
        String token = jwtService.generateToken(username, role);

        // Then
        // Manually verify token structure without exposing internal method
        SecretKey key = Keys.hmacShaKeyFor("myTestSecretKeyThatIsLongEnoughForHMACAlgorithm".getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}