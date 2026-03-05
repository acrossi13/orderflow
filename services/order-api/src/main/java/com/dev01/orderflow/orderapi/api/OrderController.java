package com.dev01.orderflow.orderapi.api;

import com.dev01.orderflow.orderapi.api.dto.CreateOrderRequest;
import com.dev01.orderflow.orderapi.api.dto.OrderResponse;
import com.dev01.orderflow.orderapi.api.dto.UpdateOrderStatusRequest;
import com.dev01.orderflow.orderapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Orders", description = "Operações de pedidos")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }


    @Operation(summary = "Criar pedido", description = "Cria um pedido com status CREATED.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Request inválida")
    })
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

    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(
            @Parameter(description = "ID do pedido", example = "e6cd81cd-997a-4595-9417-2fa779fab341")
            @PathVariable String id) {
        var order = service.getById(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @Operation(summary = "Listar pedidos", description = "Lista paginada. Use page/size/sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping
    public Page<OrderResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.list(pageable).map(OrderResponse::from);
    }

    @Operation(summary = "Atualizar status do pedido",
                description = "Regras: CREATED -> APPROVED|CANCELED. APPROVED não pode virar CANCELED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Transição inválida"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @Parameter(description = "ID do pedido")
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest req
    ) {
        var updated = service.updateStatus(id, req.status());
        return ResponseEntity.ok(OrderResponse.from(updated));
    }
}