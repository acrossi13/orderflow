package com.dev01.orderflow.orderapi.infra.events;

import com.dev01.orderflow.orderapi.domain.events.OrderEventPublisher;
import com.dev01.orderflow.orderapi.domain.events.OrderStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("local")
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
        try {
            String body = objectMapper.writeValueAsString(event);
            log.info("publishing_to_sqs queue={} body={}", queueName, body);
            sqsTemplate.send(to -> to.queue(queueName).payload(body));
            log.info("published_to_sqs queue={}", queueName);
        } catch (Exception e) {
            log.error("failed_to_publish_sqs", e);
            throw new RuntimeException("Failed to serialize OrderStatusChangedEvent", e);
        }
    }
}