package org.example.pruebatecnicaecommerce.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.CreateOrderRequest;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderStatusHistoryResponse;
import org.example.pruebatecnicaecommerce.application.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderService createOrderService;
    private final PayOrderService payOrderService;
    private final ShipOrderService shipOrderService;
    private final CancelOrderService cancelOrderService;
    private final GetOrderService getOrderService;
    private final ListOrdersService listOrdersService;
    private final GetOrderStatusService getOrderStatusService;
    private final GetOrderHistoryService getOrderHistoryService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-Customer-Id") String customerId) {
        OrderResponse response = createOrderService.execute(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{publicOrderId}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable String publicOrderId) {
        OrderResponse response = payOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{publicOrderId}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable String publicOrderId) {
        OrderResponse response = shipOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{publicOrderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String publicOrderId) {
        OrderResponse response = cancelOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicOrderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String publicOrderId) {
        OrderResponse response = getOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders() {
        List<OrderResponse> response = listOrdersService.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicOrderId}/status")
    public ResponseEntity<String> getOrderStatus(@PathVariable String publicOrderId) {
        String status = getOrderStatusService.execute(publicOrderId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{publicOrderId}/history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getOrderHistory(@PathVariable String publicOrderId) {
        List<OrderStatusHistoryResponse> history = getOrderHistoryService.execute(publicOrderId);
        return ResponseEntity.ok(history);
    }
}