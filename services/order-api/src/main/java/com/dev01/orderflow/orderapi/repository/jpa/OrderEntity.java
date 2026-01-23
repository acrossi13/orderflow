package com.dev01.orderflow.orderapi.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private String id;

    @Column(name = "customer_code", nullable = false, length = 100)
    private String customerCode;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    protected OrderEntity(){}

    public OrderEntity(String id, String customerCode, Integer amount, String status, Instant createdAt) {
        this.id = id;
        this.customerCode = customerCode;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCustomerCode() { return customerCode; }
    public Integer getAmount() { return amount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
