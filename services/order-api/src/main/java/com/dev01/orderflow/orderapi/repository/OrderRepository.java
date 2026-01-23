package com.dev01.orderflow.orderapi.repository;

import com.dev01.orderflow.orderapi.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);
    Optional<Order> findById(String id);
    Page<Order> findAll(Pageable pageable);
}