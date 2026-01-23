package com.dev01.orderflow.orderapi.service;

import com.dev01.orderflow.orderapi.api.errors.OrderNotFoundException;
import com.dev01.orderflow.orderapi.domain.Order;
import com.dev01.orderflow.orderapi.repository.OrderRepository;
import org.junit.jupiter.api.Test;

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
}
