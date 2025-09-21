package org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper;

import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.OrderEntity;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.OrderItemEntity;

import java.util.UUID;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setPublicId(order.getPublicId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setItems(order.getItems().stream()
                .map(item -> toItemEntity(item, entity))
                .collect(Collectors.toList()));
        return entity;
    }

    public static void updateEntity(OrderEntity entity, Order order) {
        entity.setCustomerId(order.getCustomerId());
        entity.setPublicId(order.getPublicId());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());

        entity.getItems().clear();
        entity.getItems().addAll(order.getItems().stream()
                .map(item -> toItemEntity(item, entity))
                .collect(Collectors.toList()));
    }

    public static Order toDomain(OrderEntity entity) {
        Order order = Order.restore(
                entity.getId(),
                entity.getPublicId(),
                entity.getCustomerId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getVersion());

        entity.getItems().forEach(item -> order.restoreItem(new OrderItem(
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice())));

        return order;
    }

    private static OrderItemEntity toItemEntity(OrderItem item, OrderEntity orderEntity) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrder(orderEntity);
        entity.setProductId(item.getProductId());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        return entity;
    }
}
