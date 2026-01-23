package com.dev01.orderflow.orderapi.api;

import com.dev01.orderflow.orderapi.api.dto.CreateOrderRequest;
import com.dev01.orderflow.orderapi.api.dto.OrderResponse;
import com.dev01.orderflow.orderapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        // evita a treta de anotações no tipo durante overload resolution
        String customerCode = req.customerCode();
        Integer amount = req.amount();

        var created = service.create(customerCode, amount);

        return ResponseEntity
                .created(URI.create("/orders/" + created.id()))
                .body(OrderResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable String id) {
        var order = service.getById(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping
    public Page<OrderResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.list(pageable).map(OrderResponse::from);
    }
}