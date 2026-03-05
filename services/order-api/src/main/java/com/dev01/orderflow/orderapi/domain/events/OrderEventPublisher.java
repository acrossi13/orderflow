package com.dev01.orderflow.orderapi.domain.events;

public interface OrderEventPublisher {
    void publish(OrderStatusChangedEvent event);
}
