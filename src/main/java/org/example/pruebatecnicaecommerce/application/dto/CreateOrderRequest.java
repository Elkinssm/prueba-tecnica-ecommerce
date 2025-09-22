package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Value
public class CreateOrderRequest {

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    List<ItemRequest> items;
}
