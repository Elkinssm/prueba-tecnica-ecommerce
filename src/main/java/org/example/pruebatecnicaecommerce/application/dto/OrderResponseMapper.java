package org.example.pruebatecnicaecommerce.application.dto;

import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseMapper {

    public static OrderResponse fromDomain(Order order) {
        List<ItemResponse> items = order.getItems().stream()
                .map(OrderResponseMapper::fromDomainItem)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getPublicId(),
                order.getCustomerId().toString(),
                order.getStatus().name(),
                order.getCreatedAt(),
                items,
                order.getTotal());
    }

    private static ItemResponse fromDomainItem(OrderItem item) {
        return new ItemResponse(
                item.getProductId().toString(),
                item.getQuantity(),
                item.getUnitPrice());
    }
}
