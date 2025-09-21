package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Value
public class ItemResponse {

    @NotNull
    String productId;

    @Positive
    int quantity;

    @DecimalMin("0.01")
    BigDecimal unitPrice;
}
