package com.dev01.orderflow.orderapi.infra.events;

import com.dev01.orderflow.orderapi.domain.events.OrderEventPublisher;
import com.dev01.orderflow.orderapi.domain.events.OrderStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "app.events.publisher", havingValue = "sqs")
@Component
public class SqsOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SqsOrderEventPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;
    private final String queueName;

    public SqsOrderEventPublisher(
            SqsTemplate sqsTemplate,
            ObjectMapper objectMapper,
            @Value("${app.sqs.order-events-queue}") String queueName
    ) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    @Override
    public void publish(OrderStatusChangedEvent event) {
        final String body;
        try {
            body = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("failed_to_serialize_event event={}", event, e);
            throw new RuntimeException("Failed to serialize OrderStatusChangedEvent", e);
        }

        try {
            log.info("publishing_to_sqs queue={} orderId={} from={} to={}",
                    queueName, event.orderId(), event.from(), event.to());

            sqsTemplate.send(to -> to
                    .queue(queueName)
                    .payload(body)
                    // atributos (não FIFO) — ajudam debug/filters/etc
                    .header("eventType", "OrderStatusChanged")
                    .header("orderId", event.orderId())
                    .header("from", String.valueOf(event.from()))
                    .header("to", String.valueOf(event.to()))
            );

            log.info("published_to_sqs queue={} orderId={}", queueName, event.orderId());
        } catch (Exception e) {
            log.error("failed_to_publish_sqs queue={} orderId={}", queueName, event.orderId(), e);
            throw new RuntimeException("Failed to publish OrderStatusChangedEvent to SQS", e);
        }
    }
}