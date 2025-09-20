package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ItemRequest {
    String productId;
    int quantity;
    BigDecimal unitPrice;
}
