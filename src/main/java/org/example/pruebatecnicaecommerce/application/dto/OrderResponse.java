package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
public class OrderResponse {

    @NotBlank
    String id;

    @NotBlank
    String customerId;

    @NotBlank
    String status;

    @NotNull
    Instant orderDate;

    @Valid
    @NotEmpty
    List<ItemResponse> items;

    @NotNull
    @PositiveOrZero
    BigDecimal total;
}
