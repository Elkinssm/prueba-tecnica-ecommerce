package org.example.pruebatecnicaecommerce.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChangeOrderStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;
}