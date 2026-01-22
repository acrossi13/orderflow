package com.dev01.orderflow.orderapi.domain;

import java.time.Instant;

public record Order(
        String id,
        String customerCode,
        Integer amount,
        OrderStatus status,
        Instant createdAt
) {}
