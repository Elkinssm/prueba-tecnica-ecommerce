package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.domain.model.user.User;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.UserMapper;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.user.UserEntity;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.user.JpaUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserJpaRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity;

        if (user.getId() != null) {
            entity = jpaRepository.findById(user.getId())
                    .orElseGet(() -> UserMapper.toEntity(user));

            UserMapper.updateEntity(entity, user);
        } else {
            entity = UserMapper.toEntity(user);
        }

        UserEntity saved = jpaRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return jpaRepository.findById(userId)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByPublicId(String publicId) {
        return jpaRepository.findByPublicId(publicId)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toList());
    }
}