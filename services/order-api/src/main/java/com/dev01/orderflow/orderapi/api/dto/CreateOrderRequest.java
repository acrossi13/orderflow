package com.dev01.orderflow.orderapi.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest (
        @NotBlank String customerCode,
        @NotNull @Min(1) Integer amount
) {}