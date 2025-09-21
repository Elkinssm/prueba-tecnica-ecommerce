package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Value
public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required")
    @Pattern(regexp = UuidUtils.UUID_REGEX, message = "Customer ID must be a valid UUID")
    String customerId;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    List<ItemRequest> items;
}
