package com.dev01.orderflow.orderapi.domain.events;

import com.dev01.orderflow.orderapi.domain.OrderStatus;

import java.time.Instant;

public record OrderStatusChangedEvent(
        String orderId,
        OrderStatus from,
        OrderStatus to,
        Instant occurredAt
) {
}
