package com.dev01.orderflow.orderapi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest (
        @Schema(example = "CUST-001")
        @NotBlank String customerCode,
        @Schema(description = "Valor do pedido (mínimo 1)", example = "10", minimum = "1")
        @NotNull @Min(1) Integer amount
) {}