package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class OrderResponse {
    String id;
    String customerId;
    String status;
    Instant orderDate;
    List<ItemResponse> items;
}
