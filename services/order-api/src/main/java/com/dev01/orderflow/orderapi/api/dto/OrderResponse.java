package com.dev01.orderflow.orderapi.api.dto;

import com.dev01.orderflow.orderapi.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record OrderResponse(
        @Schema(example = "e6cd81cd-997a-4595-9417-2fa779fab341")
        String id,
        @Schema(example = "CUST-001")
        String customerCode,
        @Schema(example = "10")
        Integer amount,
        @Schema(example = "CREATED")
        String status,
        @Schema(example = "2026-03-05T16:39:45.687294Z")
        Instant createdAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.id(), o.customerCode(), o.amount(), o.status().name(), o.createdAt());
    }
}
