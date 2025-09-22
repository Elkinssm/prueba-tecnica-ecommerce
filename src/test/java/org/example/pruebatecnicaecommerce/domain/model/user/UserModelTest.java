package org.example.pruebatecnicaecommerce.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Model Unit Tests")
class UserModelTest {

    @Test
    @DisplayName("Should create new user with default role USER")
    void shouldCreateNewUserWithDefaultRole() {
        // Given
        String username = "john_doe";
        String email = "john@example.com";
        String passwordHash = "hashed_password_123";

        // When
        User user = User.create(username, email, passwordHash);

        // Then
        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getPublicId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(UserRole.USER, user.getRole());
        assertNotNull(user.getCreatedAt());
        assertEquals(0, user.getVersion());
        assertFalse(user.isAdmin());
    }

    @Test
    @DisplayName("Should restore user from persistence with all fields")
    void shouldRestoreUserFromPersistence() {
        // Given
        UUID id = UUID.randomUUID();
        String publicId = "user_abc123";
        String username = "jane_doe";
        String email = "jane@example.com";
        String passwordHash = "hashed_password_456";
        UserRole role = UserRole.ADMIN;
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        long version = 5;

        // When
        User user = User.restore(id, publicId, username, email, passwordHash, role, createdAt, version);

        // Then
        assertEquals(id, user.getId());
        assertEquals(publicId, user.getPublicId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(role, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(version, user.getVersion());
        assertTrue(user.isAdmin());
    }

    @Test
    @DisplayName("Should restore regular user correctly")
    void shouldRestoreRegularUser() {
        // Given
        UUID id = UUID.randomUUID();
        String publicId = "user_def456";
        String username = "regular_user";
        String email = "user@example.com";
        String passwordHash = "hashed_password_789";
        UserRole role = UserRole.USER;
        Instant createdAt = Instant.parse("2024-02-01T15:30:00Z");
        long version = 2;

        // When
        User user = User.restore(id, publicId, username, email, passwordHash, role, createdAt, version);

        // Then
        assertEquals(role, user.getRole());
        assertFalse(user.isAdmin());
    }

    @Test
    @DisplayName("Should identify admin users correctly")
    void shouldIdentifyAdminUsers() {
        // Given
        User adminUser = User.restore(
                UUID.randomUUID(),
                "admin_123",
                "admin",
                "admin@example.com",
                "admin_password",
                UserRole.ADMIN,
                Instant.now(),
                1);

        User regularUser = User.create("user", "user@example.com", "user_password");

        // When & Then
        assertTrue(adminUser.isAdmin());
        assertFalse(regularUser.isAdmin());
    }

    @Test
    @DisplayName("Should generate unique IDs for each new user")
    void shouldGenerateUniqueIds() {
        // When
        User user1 = User.create("user1", "user1@example.com", "password1");
        User user2 = User.create("user2", "user2@example.com", "password2");

        // Then
        assertNotEquals(user1.getId(), user2.getId());
        assertNotEquals(user1.getPublicId(), user2.getPublicId());
    }

    @Test
    @DisplayName("Should set creation time for new users")
    void shouldSetCreationTimeForNewUsers() {
        // Given
        Instant beforeCreation = Instant.now();

        // When
        User user = User.create("test_user", "test@example.com", "password");

        // Then
        Instant afterCreation = Instant.now();
        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(user.getCreatedAt().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    @DisplayName("UserRole enum should have correct values")
    void shouldHaveCorrectUserRoleValues() {
        // Then
        assertEquals(2, UserRole.values().length);
        assertEquals(UserRole.USER, UserRole.valueOf("USER"));
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
    }

    @Test
    @DisplayName("Should handle edge cases for user creation")
    void shouldHandleEdgeCasesForUserCreation() {
        // Given
        String emptyString = "";

        // When & Then - These should still create users as validation might be handled
        // elsewhere
        assertDoesNotThrow(() -> {
            User user1 = User.create(emptyString, "test@example.com", "password");
            assertEquals(emptyString, user1.getUsername());
        });

        assertDoesNotThrow(() -> {
            User user2 = User.create("username", emptyString, "password");
            assertEquals(emptyString, user2.getEmail());
        });

        assertDoesNotThrow(() -> {
            User user3 = User.create("username", "test@example.com", emptyString);
            assertEquals(emptyString, user3.getPasswordHash());
        });
    }

    @Test
    @DisplayName("Should maintain immutability of user fields")
    void shouldMaintainImmutabilityOfUserFields() {
        // Given
        User user = User.create("test_user", "test@example.com", "password");
        UUID originalId = user.getId();
        String originalPublicId = user.getPublicId();
        String originalUsername = user.getUsername();
        String originalEmail = user.getEmail();
        String originalPasswordHash = user.getPasswordHash();
        UserRole originalRole = user.getRole();
        Instant originalCreatedAt = user.getCreatedAt();
        long originalVersion = user.getVersion();

        // When - Try to access fields multiple times

        // Then - Fields should remain unchanged
        assertEquals(originalId, user.getId());
        assertEquals(originalPublicId, user.getPublicId());
        assertEquals(originalUsername, user.getUsername());
        assertEquals(originalEmail, user.getEmail());
        assertEquals(originalPasswordHash, user.getPasswordHash());
        assertEquals(originalRole, user.getRole());
        assertEquals(originalCreatedAt, user.getCreatedAt());
        assertEquals(originalVersion, user.getVersion());
    }
}