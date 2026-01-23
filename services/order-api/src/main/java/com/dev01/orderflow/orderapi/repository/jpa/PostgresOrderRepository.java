package com.dev01.orderflow.orderapi.repository.jpa;

import com.dev01.orderflow.orderapi.domain.Order;
import com.dev01.orderflow.orderapi.domain.OrderStatus;
import com.dev01.orderflow.orderapi.repository.OrderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("local")
public class PostgresOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpa;

    public PostgresOrderRepository(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        var entity = new OrderEntity(
                order.id(),
                order.customerCode(),
                order.amount(),
                order.status().name(),
                order.createdAt()
        );
        var saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    private Order toDomain(OrderEntity e) {
        return new Order(
                e.getId(),
                e.getCustomerCode(),
                e.getAmount(),
                OrderStatus.valueOf(e.getStatus()),
                e.getCreatedAt()
        );
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(this::toDomain);
    }
}