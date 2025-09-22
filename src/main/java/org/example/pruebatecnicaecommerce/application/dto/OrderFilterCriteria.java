package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderFilterCriteria {
    private String customerId;
    private String status;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String search;
}