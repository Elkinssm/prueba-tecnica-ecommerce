package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;

import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.OrderMapper;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.JpaOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderJpaRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaRepository;

    @Override
    public void save(Order order) {
        jpaRepository.save(OrderMapper.toEntity(order));
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaRepository.findById(orderId).map(OrderMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId)
                .stream()
                .map(OrderMapper::toDomain)
                .toList();
    }
}
