package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.CreateOrderRequest;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateOrderService {

    private final OrderRepository orderRepository;

    public OrderResponse execute(CreateOrderRequest request) {
        Order order = Order.create(UuidUtils.fromString(request.getCustomerId()));

        request.getItems().forEach(item -> order.addItem(new OrderItem(
                UuidUtils.fromString(item.getProductId()),
                item.getQuantity(),
                item.getUnitPrice())));

        Order savedOrder = orderRepository.save(order);
        return OrderResponseMapper.fromDomain(savedOrder);
    }
}
