package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Value
public class ItemRequest {

    @NotBlank(message = "Product code is required")
    String productCode; // Cambio: ahora usa código amigable como PROD-11111111

    @Positive(message = "Quantity must be positive")
    int quantity;

    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    BigDecimal unitPrice;
}
