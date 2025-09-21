package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderStatusHistoryResponse {
    private String status;
    private LocalDateTime changedAt;
    private String previousStatus;
}