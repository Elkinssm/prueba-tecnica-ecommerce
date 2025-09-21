package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.OrderMapper;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.OrderEntity;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.JpaOrderRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderJpaRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaRepository;

    @Override
    public Order save(Order order) {
        OrderEntity entity;

        if (order.getId() != null) {
            entity = jpaRepository.findById(order.getId())
                    .orElseGet(() -> OrderMapper.toEntity(order));

            OrderMapper.updateEntity(entity, order);
        } else {
            entity = OrderMapper.toEntity(order);
        }

        OrderEntity saved = jpaRepository.save(entity);
        return OrderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(OrderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByPublicId(String publicId) {
        return jpaRepository.findByPublicId(publicId)
                .map(OrderMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(OrderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(OrderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(OrderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByCreatedAtBetween(Instant start, Instant end) {
        return jpaRepository.findByCreatedAtBetween(start, end).stream()
                .map(OrderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status) {
        return jpaRepository.findByCustomerIdAndStatus(customerId, status).stream()
                .map(OrderMapper::toDomain)
                .collect(Collectors.toList());
    }
}
