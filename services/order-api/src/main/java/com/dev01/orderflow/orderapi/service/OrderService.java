package com.dev01.orderflow.orderapi.service;

import com.dev01.orderflow.orderapi.api.errors.OrderNotFoundException;
import com.dev01.orderflow.orderapi.domain.Order;
import com.dev01.orderflow.orderapi.domain.OrderStatus;
import com.dev01.orderflow.orderapi.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public Order create(String customerCode, Integer amount) {
        var order = new Order(
                UUID.randomUUID().toString(),
                customerCode,
                amount,
                OrderStatus.CREATED,
                Instant.now()
        );
        return repo.save(order);
    }

    public Order getById(String id) {
        return repo.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    public Page<Order> list(Pageable pageable) {
        return repo.findAll(pageable);
    }
}