package com.dev01.orderflow.orderapi.infra.events;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class OrderEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);
    private final JdbcTemplate jdbc;

    public OrderEventsConsumer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @SqsListener("${app.sqs.order-events-queue}")
    public void onMessage(
            String message,
            @Header(name = "MessageId", required = false) String messageId
    ) {
        String eventId = (messageId != null && !messageId.isBlank())
                ? "sqs:" + messageId
                : "sha256:" + sha256Hex(message);

        if (message.contains("\"orderId\":\"X\"")) {
            throw new RuntimeException("TEST_DLQ");
        }

        int inserted = jdbc.update(
                "insert into processed_events(event_id) values (?) on conflict do nothing",
                eventId
        );

        if (inserted == 0) {
            log.info("duplicate_ignored eventId={}", eventId);
            return;
        }

        log.info("processed eventId={} payload={}", eventId, message);

        // TODO: processamento real aqui
        // Se der erro aqui: throw new RuntimeException("...") -> retry/DLQ
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("sha256_failed", e);
        }
    }
}