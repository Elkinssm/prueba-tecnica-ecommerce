package org.example.pruebatecnicaecommerce.infrastructure.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.CreateOrderRequest;
import org.example.pruebatecnicaecommerce.application.dto.OrderFilterCriteria;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderStatusHistoryResponse;
import org.example.pruebatecnicaecommerce.application.service.CancelOrderService;
import org.example.pruebatecnicaecommerce.application.service.CreateOrderService;
import org.example.pruebatecnicaecommerce.application.service.GetOrderService;
import org.example.pruebatecnicaecommerce.application.service.GetOrderStatusService;
import org.example.pruebatecnicaecommerce.application.service.GetOrderHistoryService;
import org.example.pruebatecnicaecommerce.application.service.ListOrdersService;
import org.example.pruebatecnicaecommerce.application.service.PayOrderService;
import org.example.pruebatecnicaecommerce.application.service.SearchOrdersService;
import org.example.pruebatecnicaecommerce.application.service.ShipOrderService;
import org.example.pruebatecnicaecommerce.domain.model.user.User;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRepository;
import org.example.pruebatecnicaecommerce.shared.error.ErrorResponse;
import org.example.pruebatecnicaecommerce.shared.error.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Operaciones para gestionar ordenes del ecommerce")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final CreateOrderService createOrderService;
    private final PayOrderService payOrderService;
    private final CancelOrderService cancelOrderService;
    private final ShipOrderService shipOrderService;
    private final GetOrderService getOrderService;
    private final ListOrdersService listOrdersService;
    private final GetOrderStatusService getOrderStatusService;
    private final GetOrderHistoryService getOrderHistoryService;
    private final SearchOrdersService searchOrdersService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        OrderResponse response = createOrderService.execute(request, user.getId().toString());
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

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = listOrdersService.execute();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{publicOrderId}")
    @Operation(
            summary = "Obtener detalle de una orden",
            description = "Devuelve la informacion completa de una orden"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle de orden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Identificador publico de la orden", required = true,
                    example = "ORD-20250921-OEUI")
            @PathVariable String publicOrderId) {
        OrderResponse response = getOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicOrderId}/status")
    @Operation(
            summary = "Consultar estado actual de una orden",
            description = "Devuelve el estado actual de la orden en formato texto"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actual",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "PAID"))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> getOrderStatus(
            @Parameter(description = "Identificador publico de la orden", required = true,
                    example = "ORD-20250921-OEUI")
            @PathVariable String publicOrderId) {
        String status = getOrderStatusService.execute(publicOrderId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{publicOrderId}/history")
    @Operation(
            summary = "Consultar historial de estados",
            description = "Devuelve el historial completo de cambios de estado de la orden"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de estados",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderStatusHistoryResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<OrderStatusHistoryResponse>> getOrderHistory(
            @Parameter(description = "Identificador publico de la orden", required = true,
                    example = "ORD-20250921-OEUI")
            @PathVariable String publicOrderId) {
        List<OrderStatusHistoryResponse> history = getOrderHistoryService.execute(publicOrderId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String search) {

        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .customerId(customerId)
                .status(status)
                .dateFrom(dateFrom != null ? LocalDateTime.parse(dateFrom) : null)
                .dateTo(dateTo != null ? LocalDateTime.parse(dateTo) : null)
                .search(search)
                .build();

        List<OrderResponse> orders = searchOrdersService.execute(criteria);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @PathVariable String customerId,
            @RequestParam(required = false) String status) {

        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .customerId(customerId)
                .status(status)
                .build();

        List<OrderResponse> orders = searchOrdersService.execute(criteria);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .status(status)
                .build();

        List<OrderResponse> orders = searchOrdersService.execute(criteria);
        return ResponseEntity.ok(orders);
    }
}
