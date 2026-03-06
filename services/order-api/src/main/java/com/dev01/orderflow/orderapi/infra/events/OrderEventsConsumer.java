package com.dev01.orderflow.orderapi.infra.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;

@ConditionalOnProperty(name = "app.events.publisher", havingValue = "sqs")
@Component
public class OrderEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public OrderEventsConsumer(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @SqsListener("${app.sqs.order-events-queue}")
    public void onMessage(Message<String> msg) {
        long startedAtMs = System.currentTimeMillis();

        String message = msg.getPayload();
        MessageHeaders h = msg.getHeaders();

        // spring-cloud-aws coloca o MessageId aqui (às vezes String, às vezes UUID -> toString resolve)
        String messageId = toStringOrNull(h.get("id"));

        Integer receiveCount = toInt(h.get("Sqs_Msa_ApproximateReceiveCount"));
        Long sentTsMs = toLong(h.get("Sqs_Msa_SentTimestamp")); // epoch ms (string no header)
        Instant receivedAt = toInstant(h.get("Sqs_ReceivedAt")); // geralmente Instant

        String eventId = (messageId != null && !messageId.isBlank())
                ? "sqs:" + messageId
                : "sha256:" + sha256Hex(message);

        long sqsLatencyMs = (sentTsMs != null) ? (startedAtMs - sentTsMs) : -1;

        log.info("consume_start eventId={} messageId={} receiveCount={} sqsLatencyMs={} bytes={}",
                eventId, messageId, receiveCount, sqsLatencyMs, message.length());

        try {
            // TEST only (remover depois)
            if (message.contains("\"orderId\":\"X\"")) {
                throw new RuntimeException("TEST_DLQ");
            }

            Instant occurredAt = extractOccurredAt(message);
            Instant sentAt = (sentTsMs != null) ? Instant.ofEpochMilli(sentTsMs) : null;

            int inserted = jdbc.update("""
                insert into processed_events(
                  event_id, occurred_at, source_message_id, payload, sqs_sent_at, sqs_received_at
                )
                values (?, ?, ?, ?::jsonb, ?, ?)
                on conflict do nothing
                """,
                    eventId,
                    toTimestamp(occurredAt),
                    messageId,
                    message,
                    toTimestamp(sentAt),
                    toTimestamp(receivedAt)
            );

            if (inserted == 0) {
                log.info("duplicate_ignored eventId={}", eventId);
                return;
            }

            log.info("processed eventId={} occurredAt={} sentAt={} receivedAt={} messageId={}",
                    eventId, occurredAt, sentAt, receivedAt, messageId);

            // TODO processamento real

        } catch (Exception e) {
            log.error("consume_fail eventId={} messageId={}", eventId, messageId, e);
            throw e;
        } finally {
            log.info("consume_end eventId={} tookMs={}", eventId, (System.currentTimeMillis() - startedAtMs));
        }
    }

    private Instant extractOccurredAt(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            JsonNode occurredAtNode = node.get("occurredAt");
            if (occurredAtNode == null || occurredAtNode.isNull()) return null;

            String iso = occurredAtNode.asText(null);
            if (iso == null || iso.isBlank()) return null;

            return Instant.parse(iso);
        } catch (Exception e) {
            log.warn("occurredAt_parse_failed payload={}", message, e);
            return null;
        }
    }

    private static Timestamp toTimestamp(Instant i) {
        return (i == null) ? null : Timestamp.from(i);
    }

    private static String toStringOrNull(Object v) {
        return (v == null) ? null : v.toString();
    }

    private static Integer toInt(Object v) {
        if (v == null) return null;
        return Integer.valueOf(v.toString());
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        return Long.valueOf(v.toString());
    }

    private static Instant toInstant(Object v) {
        if (v == null) return null;
        if (v instanceof Instant i) return i;
        // fallback: tenta parse ISO
        return Instant.parse(v.toString());
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