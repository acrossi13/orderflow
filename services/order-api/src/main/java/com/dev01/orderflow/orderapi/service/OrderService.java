package com.dev01.orderflow.orderapi.service;

import com.dev01.orderflow.orderapi.api.errors.InvalidOrderStatusTransitionException;
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
    public Order updateStatus(String id, String newStatusRaw) {
        var order = getById(id);

        OrderStatus from = order.status();
        OrderStatus to;
        try {
            to = OrderStatus.valueOf(newStatusRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusTransitionException(from.name(), newStatusRaw);
        }

        if (!isAllowed(from, to)) {
            throw new InvalidOrderStatusTransitionException(from.name(), to.name());
        }

        if (from == to) return order;

        var updated = new com.dev01.orderflow.orderapi.domain.Order(
                order.id(),
                order.customerCode(),
                order.amount(),
                to,
                order.createdAt()
        );

        return repo.save(updated);
    }

    private boolean isAllowed(OrderStatus from, OrderStatus to) {
        if (from == to) return true;
        return switch (from) {
            case CREATED -> (to == OrderStatus.APPROVED || to == OrderStatus.CANCELED);
            case APPROVED -> false;
            case CANCELED -> false;
        };
    }
}