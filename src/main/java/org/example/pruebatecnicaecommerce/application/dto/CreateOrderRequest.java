package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Value;

import java.util.List;

@Value
public class CreateOrderRequest {

    String customerId;
    List<ItemRequest>  items;
}
