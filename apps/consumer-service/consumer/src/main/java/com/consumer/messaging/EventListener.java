package com.consumer.messaging;

import com.consumer.domain.EventEntity;
import com.consumer.domain.EventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EventListener {

    private final EventRepository repo;

    public EventListener(EventRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(topics = "events", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String value) {
        EventEntity e = new EventEntity();
        e.setPayload(value);
        e.setTs(Instant.now());
        repo.save(e);
    }
}
