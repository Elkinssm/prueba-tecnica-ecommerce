package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.OrderFilterCriteria;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchOrdersService {

    private final OrderRepository orderRepository;

    public List<OrderResponse> execute(OrderFilterCriteria criteria) {
        List<Order> orders;

        // Filtrar por cliente y estado específicos
        if (criteria.getCustomerId() != null && criteria.getStatus() != null) {
            UUID customerId = UuidUtils.fromString(criteria.getCustomerId());
            OrderStatus status = OrderStatus.valueOf(criteria.getStatus());
            orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        }
        // Filtrar solo por cliente
        else if (criteria.getCustomerId() != null) {
            UUID customerId = UuidUtils.fromString(criteria.getCustomerId());
            orders = orderRepository.findByCustomerId(customerId);
        }
        // Filtrar solo por estado
        else if (criteria.getStatus() != null) {
            OrderStatus status = OrderStatus.valueOf(criteria.getStatus());
            orders = orderRepository.findByStatus(status);
        }
        // Filtrar por rango de fechas
        else if (criteria.getDateFrom() != null && criteria.getDateTo() != null) {
            orders = orderRepository.findByCreatedAtBetween(
                    criteria.getDateFrom().atZone(ZoneId.systemDefault()).toInstant(),
                    criteria.getDateTo().atZone(ZoneId.systemDefault()).toInstant());
        } else {
            orders = orderRepository.findAll();
        }

        // Aplicar filtro de búsqueda general si existe
        if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
            final String searchTerm = criteria.getSearch().toLowerCase();
            orders = orders.stream()
                    .filter(order -> order.getPublicId().toLowerCase().contains(searchTerm) ||
                            order.getStatus().name().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        return orders.stream()
                .map(OrderResponseMapper::fromDomain)
                .collect(Collectors.toList());
    }
}