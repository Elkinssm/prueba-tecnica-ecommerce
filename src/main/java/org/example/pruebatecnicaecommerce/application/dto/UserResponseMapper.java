package org.example.pruebatecnicaecommerce.application.dto;

import org.example.pruebatecnicaecommerce.domain.model.user.User;

public class UserResponseMapper {

    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getPublicId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name());
    }
}