package com.consumer.messaging;
/*
import com.consumer.domain.EventEntity;
import com.consumer.domain.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class EventListener {

    private final EventRepository repo;
    private final ObjectMapper mapper;          // <-- injected Spring mapper (has JavaTimeModule)

    private final Counter processed;
    private final Counter duplicates;
    private final Counter invalid;              // track bad payloads
    private final Timer latencyTimer;

    public EventListener(EventRepository repo, MeterRegistry registry, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;

        this.processed = Counter.builder("consumer_events_processed_total")
                .description("Events successfully persisted by the consumer")
                .register(registry);
        this.duplicates = Counter.builder("consumer_events_duplicate_total")
                .description("Events dropped due to duplicate messageId")
                .register(registry);
        this.invalid = Counter.builder("consumer_events_invalid_total")
                .description("Events dropped due to invalid/missing fields")
                .register(registry);
        this.latencyTimer = Timer.builder("consumer_event_latency_seconds")
                .description("End-to-end latency from producer sentAt to consumer receivedAt")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.9, 0.99)
                .register(registry);
    }

    public static class EventDto {
        public String messageId;
        public String userId;
        public String action;
        public Instant sentAt;   // Spring’s injected ObjectMapper understands this if jsr310 is on classpath
    }

    @KafkaListener(topics = "events", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String json) throws Exception {
        EventDto e = mapper.readValue(json, EventDto.class);

        // Basic validation / defaults (avoid nulls breaking DB)
        if (e.userId == null || e.userId.isBlank() || e.action == null || e.action.isBlank()) {
            invalid.increment();               // count bad payloads
            return;                            // drop (or send to DLT if configured)
        }
        if (e.messageId == null || e.messageId.isBlank()) {
            e.messageId = UUID.randomUUID().toString();  // optional, but keeps idempotency path happy
        }

        Instant receivedAt = Instant.now();
        Instant sentAt = (e.sentAt != null ? e.sentAt : receivedAt);

        var entity = new EventEntity();
        entity.setMessageId(e.messageId);
        entity.setUserId(e.userId);
        entity.setAction(e.action);
        entity.setSentAt(sentAt);
        entity.setReceivedAt(receivedAt);

        try {
            repo.save(entity);
            processed.increment();
            latencyTimer.record(Duration.between(sentAt, receivedAt));
        } catch (DataIntegrityViolationException dup) {
            duplicates.increment();            // same messageId seen before
        }
    }
}
*/