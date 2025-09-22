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
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderStatusHistoryResponse;
import org.example.pruebatecnicaecommerce.application.service.CancelOrderService;
import org.example.pruebatecnicaecommerce.application.service.CreateOrderService;
import org.example.pruebatecnicaecommerce.application.service.GetOrderHistoryService;
import org.example.pruebatecnicaecommerce.application.service.GetOrderService;
import org.example.pruebatecnicaecommerce.application.service.GetOrderStatusService;
import org.example.pruebatecnicaecommerce.application.service.ListOrdersService;
import org.example.pruebatecnicaecommerce.application.service.PayOrderService;
import org.example.pruebatecnicaecommerce.application.service.ShipOrderService;
import org.example.pruebatecnicaecommerce.shared.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Operaciones para gestionar ordenes del ecommerce")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Crear una nueva orden",
            description = "Crea una orden asociada al cliente indicado, reserva inventario y devuelve el detalle completo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventario o cliente no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de concurrencia o estado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @Parameter(name = "X-Customer-Id", description = "Identificador publico del cliente", in = ParameterIn.HEADER,
                    required = true, example = "8d66128e-760c-4c46-9f37-615f58b4b4f4")
            @RequestHeader("X-Customer-Id") String customerId) {
        OrderResponse response = createOrderService.execute(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{publicOrderId}/pay")
    @Operation(
            summary = "Marcar orden como pagada",
            description = "Actualiza el estado de la orden a PAID una vez confirmado el pago"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden pagada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Transicion de estado no valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> payOrder(
            @Parameter(description = "Identificador publico de la orden", required = true,
                    example = "ORD-20250921-OEUI")
            @PathVariable String publicOrderId) {
        OrderResponse response = payOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{publicOrderId}/ship")
    @Operation(
            summary = "Despachar orden",
            description = "Marca la orden como enviada una vez que se entrega al operador logistico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden despachada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Transicion de estado no valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> shipOrder(
            @Parameter(description = "Identificador publico de la orden", required = true,
                    example = "ORD-20250921-OEUI")
            @PathVariable String publicOrderId) {
        OrderResponse response = shipOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{publicOrderId}/cancel")
    @Operation(
            summary = "Cancelar orden",
            description = "Cancela la orden y libera cualquier inventario reservado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden cancelada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Transicion de estado no valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Identificador publico de la orden", required = true,
                    example = "ORD-20250921-OEUI")
            @PathVariable String publicOrderId) {
        OrderResponse response = cancelOrderService.execute(publicOrderId);
        return ResponseEntity.ok(response);
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

    @GetMapping
    @Operation(
            summary = "Listar ordenes",
            description = "Obtiene todas las ordenes disponibles para el usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de ordenes",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))))
    })
    public ResponseEntity<List<OrderResponse>> listOrders() {
        List<OrderResponse> response = listOrdersService.execute();
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
}
