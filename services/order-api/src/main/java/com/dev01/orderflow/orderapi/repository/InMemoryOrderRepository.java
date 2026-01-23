package com.dev01.orderflow.orderapi.repository;

import com.dev01.orderflow.orderapi.domain.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Profile("inmemory")
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<String, Order> store = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        store.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        var sorted = store.values().stream()
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        List<Order> slice = start >= sorted.size() ? List.of() : sorted.subList(start, end);

        return new PageImpl<>(slice, pageable, sorted.size());
    }
}
