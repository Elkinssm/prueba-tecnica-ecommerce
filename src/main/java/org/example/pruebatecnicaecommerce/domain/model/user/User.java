package org.example.pruebatecnicaecommerce.domain.model.user;

import lombok.Getter;
import org.example.pruebatecnicaecommerce.shared.utils.PublicIdGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private final String publicId;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final Instant createdAt;
    private long version;

    private User(UUID id, String publicId, String username, String email,
            String passwordHash, UserRole role, Instant createdAt, long version) {
        this.id = id;
        this.publicId = publicId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
        this.version = version;
    }

    public static User create(String username, String email, String passwordHash) {
        return new User(
                UUID.randomUUID(),
                PublicIdGenerator.generateUserId(),
                username,
                email,
                passwordHash,
                UserRole.USER,
                Instant.now(),
                0);
    }

    public static User restore(UUID id, String publicId, String username, String email,
            String passwordHash, UserRole role, Instant createdAt, long version) {
        return new User(id, publicId, username, email, passwordHash, role, createdAt, version);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}