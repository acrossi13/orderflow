package com.dev01.orderflow.orderapi.repository;

import com.dev01.orderflow.orderapi.domain.Order;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
}