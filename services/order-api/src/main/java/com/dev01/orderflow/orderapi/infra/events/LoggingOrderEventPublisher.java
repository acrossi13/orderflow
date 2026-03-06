package com.dev01.orderflow.orderapi.infra.events;

import com.dev01.orderflow.orderapi.domain.events.OrderEventPublisher;
import com.dev01.orderflow.orderapi.domain.events.OrderStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "app.events.publisher", havingValue = "log", matchIfMissing = true)
@Component
public class LoggingOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingOrderEventPublisher.class);

    @Override
    public void publish(OrderStatusChangedEvent event) {
        log.info("event=OrderStatusChanged orderId={} from={} to={} occurredAt={}",
                event.orderId(), event.from(), event.to(), event.occurredAt());
    }
}