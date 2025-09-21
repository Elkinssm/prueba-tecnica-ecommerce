package org.example.pruebatecnicaecommerce.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.CreateOrderRequest;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.service.CancelOrderService;
import org.example.pruebatecnicaecommerce.application.service.CreateOrderService;
import org.example.pruebatecnicaecommerce.application.service.PayOrderService;
import org.example.pruebatecnicaecommerce.application.service.ShipOrderService;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderService createOrderService;
    private final PayOrderService payOrderService;
    private final CancelOrderService cancelOrderService;
    private final ShipOrderService shipOrderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = createOrderService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{publicOrderId}/pay")
    public ResponseEntity<OrderResponse> pay(@PathVariable String publicOrderId) {
        OrderResponse response = payOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{publicOrderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable String publicOrderId) {
        OrderResponse response = cancelOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{publicOrderId}/ship")
    public ResponseEntity<OrderResponse> ship(@PathVariable String publicOrderId) {
        OrderResponse response = shipOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }
}
