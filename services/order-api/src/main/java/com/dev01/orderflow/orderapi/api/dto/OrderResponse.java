package com.dev01.orderflow.orderapi.api.dto;

import com.dev01.orderflow.orderapi.domain.Order;

import java.time.Instant;

public record OrderResponse(
        String id,
        String customerCode,
        Integer amount,
        String status,
        Instant createdAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.id(), o.customerCode(), o.amount(), o.status().name(), o.createdAt());
    }
}
