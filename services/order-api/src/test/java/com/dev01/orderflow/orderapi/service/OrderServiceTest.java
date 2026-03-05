package com.dev01.orderflow.orderapi.service;

import com.dev01.orderflow.orderapi.api.errors.OrderNotFoundException;
import com.dev01.orderflow.orderapi.domain.Order;
import com.dev01.orderflow.orderapi.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import com.dev01.orderflow.orderapi.api.errors.InvalidOrderStatusTransitionException;
import com.dev01.orderflow.orderapi.domain.OrderStatus;

import java.time.Instant;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void create_shouldSaveOrder() {
        OrderRepository repo = mock(OrderRepository.class);
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderService service = new OrderService(repo);

        var created = service.create("CUST-001", 10);

        assertNotNull(created.id());
        assertEquals("CUST-001", created.customerCode());
        assertEquals(10, created.amount());
        assertEquals("CREATED", created.status().name());

        verify(repo).save(any(Order.class));
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        OrderRepository repo = mock(OrderRepository.class);
        when(repo.findById("x")).thenReturn(Optional.empty());

        OrderService service = new OrderService(repo);

        assertThrows(OrderNotFoundException.class, () -> service.getById("x"));
    }

    @Test
    void updateStatus_shouldApproveFromCreated() {
        OrderRepository repo = mock(OrderRepository.class);

        var created = new Order("1", "CUST-001", 10, OrderStatus.CREATED, Instant.now());
        when(repo.findById("1")).thenReturn(Optional.of(created));
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderService service = new OrderService(repo);

        var updated = service.updateStatus("1", "APPROVED");

        assertEquals(OrderStatus.APPROVED, updated.status());
        verify(repo).save(any(Order.class));
    }

    @Test
    void updateStatus_shouldThrowOnInvalidTransition() {
        OrderRepository repo = mock(OrderRepository.class);

        var approved = new Order("1", "CUST-001", 10, OrderStatus.APPROVED, Instant.now());
        when(repo.findById("1")).thenReturn(Optional.of(approved));

        OrderService service = new OrderService(repo);

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> service.updateStatus("1", "CANCELED"));
    }
}
