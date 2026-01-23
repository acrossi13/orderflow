package com.dev01.orderflow.orderapi.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {}