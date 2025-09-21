package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Value
public class ItemRequest {

    @NotBlank(message = "Product ID is required")
    @Pattern(regexp = UuidUtils.UUID_REGEX, message = "Product ID must be a valid UUID")
    String productId;

    @Positive(message = "Quantity must be positive")
    int quantity;

    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    BigDecimal unitPrice;
}
