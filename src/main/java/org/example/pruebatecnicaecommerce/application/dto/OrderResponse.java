package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
}
