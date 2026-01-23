package com.dev01.orderflow.orderapi.api;

import com.dev01.orderflow.orderapi.domain.Order;
import com.dev01.orderflow.orderapi.domain.OrderStatus;
import com.dev01.orderflow.orderapi.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    // mocka o service porque não queremos testar o service aqui
    @MockBean
    OrderService service;

    @Test
    void postOrders_shouldReturn201() throws Exception {
        // Arrange
        when(service.create(eq("CUST-001"), eq(10)))
                .thenReturn(new Order("1", "CUST-001", 10, OrderStatus.CREATED, Instant.now()));

        // Act + Assert
        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerCode":"CUST-001","amount":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/1"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void postOrders_shouldReturn400_whenInvalid() throws Exception {
        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerCode\":\"\",\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(eq("1")))
                .thenReturn(new Order("1", "CUST-001", 10, OrderStatus.CREATED, Instant.now()));

        mvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void getOrders_shouldReturn200() throws Exception {
        when(service.list(any()))
                .thenReturn(new PageImpl<>(
                        java.util.List.of(new Order("1","CUST-001",10,OrderStatus.CREATED,Instant.now())),
                        PageRequest.of(0, 20),
                        1
                ));

        mvc.perform(get("/orders?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
