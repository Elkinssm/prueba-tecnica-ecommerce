package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.CreateOrderRequest;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateOrderService {

    private final OrderRepository orderRepository;

    public CreateOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse execute(CreateOrderRequest request) {
        Order order = Order.create(UUID.fromString(request.getCustomerId()));

        request.getItems().forEach(item ->
                order.addItem(new OrderItem(
                        UUID.fromString(item.getProductId()),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
        );

        orderRepository.save(order);

        return OrderResponseMapper.fromDomain(order);
    }


}
